# Checkpoint — schema migration verification

Date: 2026-09-19. Base commit: b7303ee. Scope: verify the Week 1–3 schema migration on a disposable SQL Server clone, preserve data, rerun migration, test constraints and Java/HTTP consumers.

## Changes and evidence

- Existing report edits and untracked `logs/` were present before this task and preserved.
- Fixed confirmed SQL Server binding failure on the second migration run: compile legacy `assignment_id` validation dynamically only when that column exists.
- Set filtered-index session options before seed statements.
- Added `tools/verify_schema_migration.py`: clone-only target guard, two migration runs, six-table row fingerprints, catalog assertions and three rollback-based negative cases with independent reads.
- Test helpers accept a separate environment file so `.env.test` is not overwritten.
- Original `AITA_Week3_Verification` remains legacy. Windows-authenticated COPY_ONLY backup/restore created `AITA_Migration_Verification_20260919` (original defect reproduction) and `AITA_Migration_Verification_20260919_Fixed` (fixed schema). Retain these and their backup files for review; no original DB schema changes.
- Fixed clone: migration twice passes; row counts and SHA-256 fingerprints match excluding the removed derived column. Both triggers enabled and CHECK enabled/trusted. Self comparison, cross-assignment report and invalid submission reassignment rejected without persisted row changes.
- `tools/test-java.ps1 -EnvironmentFile target/.env.migration.test`: 177 tests, no failures/errors/skips, real SQL Server plus unit/mock tests.
- HTTP baseline 27/27 and follow-up 48/48 pass against the migrated clone. Fixed fixture SQL error 334 using OUTPUT INTO, without disabling triggers or assertions.
- Evidence: `target/migration-AITA_Migration_Verification_20260919_Fixed/results.json`, `target/migration-run-*.txt`, `target/surefire-reports`; clone backup location recorded in `target/migration-clone.json`.

## Boundary and next action

Final closure: `database/NORMALIZATION_ANALYSIS.md` documents per-table candidate keys, functional dependencies, the legacy violation and explicit metadata/snapshot assumptions. The post-migration schema meets 3NF relative to that stated dependency set, not unconditionally.

The original test DB `AITA_Week3_Verification` was subsequently backed up COPY_ONLY and migrated in a transaction, twice. All six table fingerprints were preserved except the removed derived column. Catalog checks passed. Evidence: `target/test-db-migration-results.json`. The application database was not changed. UI/performance/Google callback/Gemini real calls were not rerun; no fresh-context independent auditor was used.

Final verification on migrated original test DB: 177/177 Java/JDBC, 27/27 baseline HTTP and 48/48 follow-up HTTP passed; WAR build passed. Task-owned Tomcat stopped; temporary environment/scratch scripts removed. Verification artifacts and database copies retained.

Delivery: selected source/doc changes are prepared for commit/push; consult Git HEAD and origin/main for publication status. User-owned untracked logs remain excluded. The two original documentation/normalization issues are complete in the stated scope, not the entire project. Application database deployment is outside this test-DB rollout.
