# AGENTS.md

## Purpose
- This document defines repository-specific guidance for autonomous coding agents.
- Follow these rules before applying general best practices.
- Prefer minimal, focused diffs that preserve existing behavior.

## Project Identity
- Name: Fluid Sieve: Rewoven (Fabric mod).
- Build system: Gradle Wrapper (`gradlew`, `gradlew.bat`).
- Language: Java.
- Source layout: `src/main/java` and `src/main/resources`.
- Main package namespace: `net.crioch.fluid_sieve`.

## Versioning and Porting Context (Read First)
- Historical baseline (old branch/work): `1.21.7` era was obfuscated and relied on Loom-centric tooling.
- Current target line: `26.1` snapshots are unobfuscated under the new versioning model.
- Current repository snapshot target: `26.1-snapshot-10`.
- Near-future migration target: `26.1-snapshot-11`.
- Important: the repo still contains Loom traces from prior work; treat these as migration residue unless explicitly needed.
- Agent policy: keep builds passing today, but prefer changes that move toward unobfuscated `26.1+` workflows.

## Mandatory mcdev MCP Initialization Rule
- If you use mcdev MCP tools for Minecraft internals in a task, initialize version first.
- Required first call: `mc_set_version` (tool name in this environment: `mcdev_mc_set_version`).
- Do this before `search`, `get_class`, `get_method`, or any other mcdev call.
- Use the task target version explicitly.
- Current default in this repo: `26.1-snapshot-10`.
- Example sequence:
  - `mcdev_mc_set_version(version="26.1-snapshot-10")`
  - then call other `mcdev_*` tools.

## Repository Layout
- `src/main/java/net/crioch/fluid_sieve/` common mod initialization.
- `src/main/java/net/crioch/fluid_sieve/client/` client-only entrypoint.
- `src/main/java/net/crioch/fluid_sieve/block/` block classes and registration.
- `src/main/java/net/crioch/fluid_sieve/item/` item classes and registration.
- `src/main/resources/assets/fluid_sieve/` lang, textures, models, blockstates, item defs.
- `src/main/resources/data/fluid_sieve/` recipes and loot tables.
- `src/main/resources/data/minecraft/` vanilla namespace data integration.

## Build, Check, and Run Commands

### Windows (primary)
- Build artifacts: `gradlew.bat build`
- Compile Java only: `gradlew.bat classes`
- Assemble outputs without full verification: `gradlew.bat assemble`
- Run verification tasks: `gradlew.bat check`
- Clean outputs: `gradlew.bat clean`
- Dev client run: `gradlew.bat runClient`
- Dev server run: `gradlew.bat runServer`

### macOS/Linux
- Build artifacts: `./gradlew build`
- Compile Java only: `./gradlew classes`
- Assemble outputs without full verification: `./gradlew assemble`
- Run verification tasks: `./gradlew check`
- Clean outputs: `./gradlew clean`
- Dev client run: `./gradlew runClient`
- Dev server run: `./gradlew runServer`

## Test Commands (Including Single-Test Invocation)
- Current status: there is no `src/test` tree and no configured test dependencies yet.
- These are the canonical Gradle commands once tests are added.

### Run all tests
- Windows: `gradlew.bat test`
- macOS/Linux: `./gradlew test`

### Run one test class
- Windows: `gradlew.bat test --tests "net.crioch.fluid_sieve.ExampleTest"`
- macOS/Linux: `./gradlew test --tests "net.crioch.fluid_sieve.ExampleTest"`

### Run one test method
- Windows: `gradlew.bat test --tests "net.crioch.fluid_sieve.ExampleTest.methodName"`
- macOS/Linux: `./gradlew test --tests "net.crioch.fluid_sieve.ExampleTest.methodName"`

### Useful Gradle flags for debugging tests
- Keep stack traces: `--stacktrace`
- More logs: `--info`
- Avoid daemon state issues in CI-like runs: `--no-daemon`

## Lint and Formatting Reality
- No dedicated lint plugin is currently configured (no Checkstyle/Spotless/PMD/ErrorProne in `build.gradle`).
- Treat compilation and runtime sanity as the effective quality gates.
- Do not introduce broad formatting-only churn.
- If you add formatter/linter tooling in a future task, update this file.

## Code Style: Formatting
- Use 4 spaces indentation.
- Use braces on the same line (K&R style).
- Use one blank line between logical code blocks.
- Keep line wrapping consistent with existing Java files.
- Avoid trailing whitespace and unrelated whitespace edits.

## Code Style: Imports
- Never use wildcard imports.
- Keep only used imports.
- Prefer stable ordering:
  - `java.*`
  - third-party and `net.*`
  - project package imports
- Avoid static imports unless they clearly improve readability.

## Code Style: Types and APIs
- Prefer explicit types where domain clarity matters (`ServerLevel`, `BlockPos`, `ItemStack`).
- Match existing style: explicit declarations are common; use `var` sparingly.
- Keep helper methods `private` unless broader access is required.
- Use immutable empty results when appropriate (`List.of()`).
- Respect Minecraft/Fabric API contracts and side distinctions.

## Naming Conventions
- Class/interface names: `PascalCase`.
- Methods/fields: `camelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- Resource/registry paths and file names: `lower_snake_case`.
- Package names: lowercase; preserve existing `fluid_sieve` naming.

## Error Handling and Safety
- Prefer guard clauses and early returns for no-op paths.
- Check runtime state before side effects (null/empty/container checks).
- Handle missing registry/data lookups gracefully.
- Avoid throwing for expected game-state edge cases.
- Do not silently swallow exceptions when adding new IO/parsing logic.

## Registry and Identifier Practices
- Centralize `id(...)`, `key(...)`, and `register(...)` helper patterns.
- Build identifiers via namespace/path helpers; avoid duplicated raw strings.
- Keep block and item registration paths consistent.
- Ensure JSON resource IDs align with registered identifiers.

## Minecraft/Fabric Structure Rules
- Keep common initialization in `ModInitializer` paths.
- Keep client-only references in `ClientModInitializer` paths.
- Never leak client-only classes into common code.
- Preserve `fabric.mod.json` entrypoint validity after refactors.

## Data and Asset Consistency Rules
- Keep recipes, loot tables, lang keys, models, and blockstates synchronized.
- Preserve namespace correctness (`fluid_sieve` vs `minecraft`).
- When adding content, ensure both item/block registration and assets are present.
- Validate path conventions before finalizing changes.

## Security and Secrets
- Never commit tokens, API keys, or credentials.
- Treat local config files (including `opencode.jsonc`) as sensitive.
- Never copy secrets from local configs into code, docs, commits, PRs, or issues.
- If sensitive data exposure is detected, stop and report it in your summary.

## Git Hygiene for Agents
- Do not revert unrelated user changes.
- Keep commits task-scoped and atomic.
- Avoid destructive git operations unless explicitly requested.
- Prefer straightforward commit history; avoid amend unless specifically instructed.

## Cursor and Copilot Rule Files
- Checked in this repo at time of writing:
  - `.cursorrules`: not present
  - `.cursor/rules/`: not present
  - `.github/copilot-instructions.md`: not present
- If any are added later, treat them as authoritative and update this file accordingly.

## Pre-Finish Validation Checklist
- Build or compile succeeds with wrapper commands.
- No accidental resource path or identifier drift.
- Entrypoints and dependency constraints remain valid.
- Diffs are minimal and directly tied to the task.
- This `AGENTS.md` remains current with tooling/version workflow.
