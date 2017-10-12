ALTER TABLE ONLY valid_reason_assignments
  ADD CONSTRAINT unq_valid_reasons
  UNIQUE(facilitytypeid, programid, reasonid);
