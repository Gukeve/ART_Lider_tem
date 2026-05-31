# Codex Instructions

Always read:

* ../AGENTS.md
* ../docs/architecture.md
* ../docs/known-bugs.md

Before editing:

1. Understand the existing code.
2. Preserve architecture.
3. Preserve BLE Mesh functionality.
4. Preserve Room database functionality.
5. Preserve Compose UI.

When fixing bugs:

* Find root cause.
* Avoid unnecessary rewrites.
* Keep API compatibility.
* Minimize code changes.

When creating commits:

* Use descriptive commit messages.
* Update documentation when architecture changes.

Never:

* Replace BLE Mesh with cloud messaging.
* Remove persistence.
* Delete working features without justification.
