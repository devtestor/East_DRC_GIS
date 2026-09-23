# Migration Rehearsal Checklist

## Source Inventory

- Identify each source database, spreadsheet, shapefile, scanned archive and paper-derived register.
- Record owner, legal authority, sensitivity, expected record count and physical or digital custody.
- Assign a migration batch identifier before extraction.

## Profiling

- Profile administrative names, parcel references, proposed UPI candidates, geometry formats and coordinate reference systems.
- Flag duplicates, missing parent parcels, invalid geometries, inconsistent areas and conflicting party names.
- Record confidence levels without resolving legal ambiguity automatically.

## Mapping

- Map source fields to target entities: administrative units, parcels, geometry versions, parties, rights, restrictions, documents and audit lineage.
- Document transformation rules and rejected fields.
- Confirm protected personal data is not included in public projections.

## Trial Migration

- Load records into a nonproduction pilot environment.
- Preserve source system, source identifier, transformation rule, validation result, confidence status and exception status.
- Run geometry validation, duplicate detection, UPI uniqueness checks and rights matching.
- Link scanned evidence by document metadata and checksum, not by storing large files in database columns.

## Reconciliation

- Compare source counts, accepted target counts, rejected records and exception queues.
- Review unresolved conflicts with authorized officials.
- Produce an acceptance report with known gaps and remediation owners.

## Cutover Readiness

- Confirm written authorization before any production import.
- Confirm backup and restore have been tested.
- Confirm rollback decision owner and stop conditions.
- Confirm migrated records remain distinguishable from newly created pilot records.
