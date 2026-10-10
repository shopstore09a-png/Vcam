# Camera Hook Engine

This package is separate from RootChecker and the existing UI.

Planned integration:
- Framework adapter for the supported Vector API.
- Framework adapter for LSPosed where compatible.
- Explicit module activation and target-app scope diagnostics.
- Camera1 and Camera2 interception only after the framework entry point is verified.
- Accurate status reporting; root access must never be treated as proof that hooks are active.

Do not report ACTIVE until a hook has actually initialized successfully in a target process.
