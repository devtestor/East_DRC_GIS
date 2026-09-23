package cd.edrc.landgis.surveys;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.parcels.CreateParcelGeometryVersionRequest;
import cd.edrc.landgis.parcels.ParcelGeometryService;
import cd.edrc.landgis.parcels.ParcelGeometryVersionResponse;
import cd.edrc.landgis.parcels.ParcelRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurveyService {
    private final ParcelRepository parcels;
    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;
    private final ParcelGeometryService geometries;

    public SurveyService(ParcelRepository parcels, JdbcTemplate jdbcTemplate, AuditService auditService,
            ParcelGeometryService geometries) {
        this.parcels = parcels;
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
        this.geometries = geometries;
    }

    @Transactional
    public SurveyResponse create(CreateSurveyRequest request, AuthenticatedActor actor) {
        if (!parcels.existsById(request.parcelId())) {
            throw new IllegalArgumentException("Parcel not found: " + request.parcelId());
        }
        UUID assignee = request.assignedToUserId() == null ? actor.userId() : request.assignedToUserId();
        SurveyResponse survey = jdbcTemplate.queryForObject(
                """
                INSERT INTO surveys.surveys (parcel_id, assigned_to_user_id, status, purpose)
                VALUES (?, ?, 'ASSIGNED', ?)
                RETURNING id, parcel_id, assigned_to_user_id, status, purpose,
                          0 AS observation_count,
                          submitted_at, created_at
                """,
                mapper(), request.parcelId(), assignee,
                request.purpose() == null || request.purpose().isBlank() ? "Cadastral survey" : request.purpose().trim());
        auditService.record("survey.assigned", "survey", survey.id().toString(), AuditClassification.STAFF_OPERATIONAL,
                actor.userId(), null, java.util.Map.of("parcelId", survey.parcelId().toString(), "assignedTo", assignee.toString()));
        return survey;
    }

    @Transactional(readOnly = true)
    public List<SurveyResponse> list(AuthenticatedActor actor) {
        return jdbcTemplate.query(
                """
                SELECT id, parcel_id, assigned_to_user_id, status, purpose,
                       (SELECT count(*) FROM surveys.observations observation WHERE observation.survey_id = survey.id) AS observation_count,
                       submitted_at, created_at
                FROM surveys.surveys survey
                WHERE assigned_to_user_id = ?
                ORDER BY created_at DESC
                LIMIT 100
                """, mapper(), actor.userId());
    }

    @Transactional
    public SurveyResponse addObservation(UUID surveyId, CreateSurveyObservationRequest request, AuthenticatedActor actor) {
        SurveyResponse survey = requireSurvey(surveyId);
        requireAssigned(survey, actor);
        if ("SUBMITTED".equals(survey.status()) || "APPROVED".equals(survey.status())) {
            throw new IllegalStateException("Submitted surveys cannot be edited");
        }
        requireActiveDevice(request.deviceId(), actor);
        jdbcTemplate.update(
                """
                INSERT INTO surveys.observations
                    (survey_id, observation_type, latitude, longitude, accuracy_meters, note, captured_by_user_id, client_observation_id,
                     device_id, capture_source, gnss_fix_quality, photo_object_key, signature_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (survey_id, client_observation_id) WHERE client_observation_id IS NOT NULL DO NOTHING
                """,
                surveyId, request.observationType().name(), request.latitude(), request.longitude(),
                request.accuracyMeters(), request.note(), actor.userId(), request.clientObservationId(), request.deviceId(),
                request.captureSource() == null || request.captureSource().isBlank() ? "MANUAL" : request.captureSource(),
                request.gnssFixQuality(), request.photoObjectKey(),
                request.signatureStatus() == null || request.signatureStatus().isBlank() ? "NOT_CAPTURED" : request.signatureStatus());
        jdbcTemplate.update("UPDATE surveys.surveys SET status = 'IN_PROGRESS', updated_at = now(), version = version + 1 WHERE id = ?", surveyId);
        auditService.record("survey.observation-captured", "survey", surveyId.toString(), AuditClassification.STAFF_OPERATIONAL,
                actor.userId(), null, java.util.Map.of("observationType", request.observationType().name()));
        return requireSurvey(surveyId);
    }

    @Transactional
    public ParcelGeometryVersionResponse submit(UUID surveyId, SubmitSurveyRequest request, AuthenticatedActor actor) {
        SurveyResponse survey = requireSurvey(surveyId);
        requireAssigned(survey, actor);
        if (!"IN_PROGRESS".equals(survey.status()) && !"ASSIGNED".equals(survey.status())) {
            throw new IllegalStateException("Survey cannot be submitted from status " + survey.status());
        }
        Integer observations = jdbcTemplate.queryForObject("SELECT count(*) FROM surveys.observations WHERE survey_id = ?", Integer.class, surveyId);
        if (observations == null || observations == 0) {
            throw new IllegalStateException("At least one field observation is required");
        }
        ParcelGeometryVersionResponse geometry = geometries.createDraftGeometry(
                survey.parcelId(), new CreateParcelGeometryVersionRequest(request.geometryWkt(), "FIELD_SURVEY:" + surveyId));
        jdbcTemplate.update("UPDATE surveys.surveys SET status = 'SUBMITTED', submitted_at = now(), submitted_by_user_id = ?, updated_at = now(), version = version + 1 WHERE id = ?",
                actor.userId(), surveyId);
        auditService.record("survey.submitted", "survey", surveyId.toString(), AuditClassification.STAFF_OPERATIONAL,
                actor.userId(), null, java.util.Map.of("geometryVersionId", geometry.id().toString()));
        return geometry;
    }

    @Transactional
    public BoundaryAcknowledgementResponse acknowledgeBoundary(UUID surveyId, BoundaryAcknowledgementRequest request,
            AuthenticatedActor actor) {
        SurveyResponse survey = requireSurvey(surveyId);
        requireAssigned(survey, actor);
        BoundaryAcknowledgementResponse response = jdbcTemplate.queryForObject(
                """
                INSERT INTO surveys.boundary_acknowledgements
                    (survey_id, neighbor_name, status, note, recorded_by_user_id)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id, survey_id, neighbor_name, status, note, acknowledged_at
                """,
                (resultSet, rowNum) -> new BoundaryAcknowledgementResponse(
                        resultSet.getObject("id", UUID.class), resultSet.getObject("survey_id", UUID.class),
                        resultSet.getString("neighbor_name"), resultSet.getString("status"),
                        resultSet.getString("note"), resultSet.getObject("acknowledged_at", java.time.OffsetDateTime.class)),
                surveyId, request.neighborName().trim(), request.status().name(), request.note(), actor.userId());
        auditService.record("survey.boundary-acknowledgement-recorded", "survey", surveyId.toString(),
                AuditClassification.LEGAL_EVIDENCE, actor.userId(), null,
                java.util.Map.of("status", request.status().name()));
        return response;
    }

    private SurveyResponse requireSurvey(UUID surveyId) {
        List<SurveyResponse> surveys = jdbcTemplate.query(
                "SELECT id, parcel_id, assigned_to_user_id, status, purpose, (SELECT count(*) FROM surveys.observations observation WHERE observation.survey_id = survey.id) AS observation_count, submitted_at, created_at FROM surveys.surveys survey WHERE id = ?",
                mapper(), surveyId);
        if (surveys.isEmpty()) throw new IllegalArgumentException("Survey not found: " + surveyId);
        return surveys.getFirst();
    }

    private void requireAssigned(SurveyResponse survey, AuthenticatedActor actor) {
        if (!survey.assignedToUserId().equals(actor.userId())) throw new IllegalArgumentException("Survey is assigned to another field officer");
    }

    private void requireActiveDevice(String deviceId, AuthenticatedActor actor) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM identity.registered_devices WHERE device_id = ? AND assigned_user_id = ? AND device_type = 'FIELD_MOBILE' AND status = 'ACTIVE' AND (expires_at IS NULL OR expires_at > now())",
                Integer.class, deviceId, actor.userId());
        if (count == null || count == 0) {
            throw new IllegalArgumentException("An active enrolled field device assigned to the actor is required");
        }
    }

    private RowMapper<SurveyResponse> mapper() {
        return (resultSet, rowNum) -> new SurveyResponse(
                resultSet.getObject("id", UUID.class), resultSet.getObject("parcel_id", UUID.class),
                resultSet.getObject("assigned_to_user_id", UUID.class), resultSet.getString("status"),
                resultSet.getString("purpose"), resultSet.getInt("observation_count"),
                resultSet.getObject("submitted_at", java.time.OffsetDateTime.class),
                resultSet.getObject("created_at", java.time.OffsetDateTime.class));
    }
}
