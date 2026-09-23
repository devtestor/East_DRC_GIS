package cd.edrc.landgis.parcels;

record ParcelTransitionDecision(boolean allowed, boolean requiresApproval) {
    static ParcelTransitionDecision denied() {
        return new ParcelTransitionDecision(false, false);
    }
}
