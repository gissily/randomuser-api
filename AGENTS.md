# AGENTS.md

## CRITICAL Development Rules

- After generating or modifying code, you MUST compile the sources to validate the changes before any testing.
- Do not use placeholder for class name, method, any key naming.
- **Naming Convention**:
  - Test methods must use `camelCase` style.
  - Other naming patterns (e.g., BDD style with `_`) must use `@DisplayName`.
  - Names must be concise and meaningful (see-name-know-meaning). Do NOT pad class/method names by transcribing spec section text or requirement IDs (e.g. `FR-007`); pick a short, accurate name.
  - Long human-readable descriptions belong in `@DisplayName`, NOT in the method name. Keep test method names short; put the full sentence in `@DisplayName`.
- Do not touch any files without required.
- **Checkstyle**: All agents MUST run checkstyle validation before committing. Never disable checkstyle rules.
- **Git Operations**: The Spec Kit workflow may mention git commands for creating features or committing code changes. This project does NOT require any git operations from agents — the user handles all git operations manually. When you encounter any step involving git (branch creation, commit, push, etc.), skip it entirely and proceed with the next step.
- **English Only**: All generated documents and code comments MUST be written in English — this includes spec/plan/tasks and every other Markdown file, as well as all Javadoc and inline comments. No exceptions.
- **Javadoc describes behavior, not spec**: Javadoc and comments MUST describe what the code IS and DOES (purpose, parameters, return, side effects). They MUST NOT reference spec artifacts (`specs/`, `spec.md`, `plan.md`, `tasks.md`, `FR-`, `SC-`, `US`), because spec documents are never published with the jar. A Javadoc that points at a spec chapter or requirement ID conveys nothing to a reader of the code.
- **README describes what/does/how**: Each module README MUST state what the module is, what it does, and how to use it. It MUST NOT reference spec artifacts. Keep it build-and-usage focused.
- **Imports over fully-qualified names**: When a referenced type is not in the same package and is not name-colliding, use an `import` statement. Do NOT write the fully-qualified class name inline inside code blocks or method bodies.
- **Lint before build**: When idea (or any lint) tools are available, agents MUST run `lint_files` to check code quality — including WARNING-level problems — before building. Fix warnings that are reasonable; if a warning is not reasonable, seek a suggestion before suppressing it. Never disable lint rules to make them pass.
- **Temporary files under /tmp**: Any temporary or throwaway file created for validation or experimentation MUST be created under `/tmp`, never inside the project working directory or the user's home directory. A throwaway consumer project for BOM import validation, for example, MUST live under `/tmp` and MUST be removed after use.

## Build & Test

- **idea_***: All the tools can help build and compile
- **Build**: `./mvnw clean compile`
- **Build Testcases**: `./mvnw clean test-compile`
- **Test All**: `./mvnw clean verify -Dgpg.skip=true`
- **Test Module**: `./mvnw clean test -pl <module>`
- **Coverage**: `./mvnw jacoco:report`
- **Test and Coverage**: `./mvnw clean verify jacoco:report -Dgpg.skip=true`
- **Test and Coverage Module**: `./mvnw clean verify jacoco:report -pl <module> -Dgpg.skip=true`

> **Checkstyle** is bound to the `validate` phase and runs automatically with every build (including `./mvnw clean verify`). Do NOT invoke the checkstyle plugin goals directly. The code you write MUST pass checkstyle validation — running `./mvnw clean verify` is how you confirm it.

## Infrastructure & Setup

- **Local Services**: None required (randomuser.me API is external).
- **API Testing**: Use seed parameter for deterministic results: `?seed=test123`
- **Rate Limiting**: Add delays between requests in tests to avoid HTTP 429.

## Project Architecture

- **Structure**: Multi-module Maven project.
  - Standard Layout: `src/main`, `src/test` per module.
- **Modules**: 
  - `randomuser-api-core`: Core interfaces, data models, default JDK HttpClient implementation.
  - `randomuser-api-openfeign`: Optional OpenFeign implementation.
- **Base Package**: `xyz.opcal.tools` (Module name is NOT repeated in package structure).
- **Tech Stack**: Java 17 LTS, Maven 3.8+, java.net.http.HttpClient (default), OpenFeign (optional).
- **SPI**: Java ServiceLoader for implementation discovery with priority ordering (JDK > OpenFeign > others).

## Project Structure

```text
randomuser-api/
├── randomuser-api-core
├── randomuser-api-openfeign
```

## Workflows & Conventions

- **Configuration**: No external config server required.
- **Lombok**: Enabled; avoid boilerplate code.
- **Records**: Use Java records for simple value objects (Name, Location, Coordinates, etc.).
- **Immutability**: All data models are immutable (final fields, no setters).
- **SPI Discovery**: Implementations registered via `META-INF/services/`.

## Coding Assistance

**MANDATORY WHEN AVAILABLE**: When performing code generation, implementation, or bug fixing, agents MUST use the "idea" MCP server tools if available. These tools provide superior code understanding, syntax checking, and formatting compared to standard file tools.

**Check Availability**: At the start of each task, verify idea MCP tools are available. If available, use them for all code-related operations. Fall back to standard file tools (read, write, edit, bash) ONLY if idea MCP is unavailable.

### Available Tool Categories

| Category | Capabilities | When to Use |
|----------|--------------|-------------|
| **Search & Navigation** | `search_symbol`, `search_text`, `search_regex`, `search_file`, `analyze_calls`, `list_directory_tree` | Finding code, understanding structure, locating symbols |
| **Read & Analyze** | `read_file`, `get_symbol_info`, `get_file_problems`, `lint_files`, `generate_psi_tree` | Reading code, checking errors, analyzing structure |
| **Edit & Refactor** | `replace_text_in_file`, `rename_refactoring`, `reformat_file`, `create_new_file`, `apply_patch` | Making changes, renaming, formatting, creating files |
| **Build & Run** | `build_project`, `execute_run_configuration`, `get_run_configurations` | Compiling, running tests/applications |
| **Project Info** | `get_project_modules`, `get_repositories`, `get_all_open_file_paths` | Understanding project structure |

**NOTE**: Tool names vary by MCP server implementation. Use the tool names provided by your MCP server configuration. The categories above show the capability names — prefix them with your server's namespace (e.g., `mcp__idea__`, `mcp__cursor__`, etc.).

### Usage Principles

1. **MANDATORY**: Use idea tools for ALL code operations when available — do NOT use standard file tools for code reading, writing, or editing
2. **Search first** — use `search_symbol` or `search_text` before reading files
3. **Build after edits** — always compile after making code changes
4. **Lint before commit** — run `lint_files` to catch issues early
5. **Use `execute_tool`** for dynamic invocation when tool name is not known in advance
6. **File Headers**: When creating new Java files, the Apache License 2.0 header MUST be present and pass checkstyle. If the available tools can generate it automatically, use them. When no such tool is available, reference the header of existing Java files in the project.

### Finding Tools

When you need a specific capability:
- Use your MCP server's tool listing function to discover available tools
- Or check your MCP server's documentation

**NOTE**: Tool names vary by MCP server implementation. Use the tool names provided by your MCP server configuration. The categories above show the capability names — prefix them with your server's namespace (e.g., `mcp__idea__`, `mcp__cursor__`, etc.). **MANDATORY**: If your MCP server is available, you MUST use it for all code operations (reading, writing, editing, searching, building). Only fall back to standard file tools (read, write, edit, bash) if your MCP server is completely unavailable (connection error, not configured, etc.).

## Coding Style Guidelines

**CRITICAL**: All AI agents MUST strictly follow these Java coding style rules during code generation and development:

- **Indentation**: Use **Tabs** (`USE_TAB_CHARACTER=true`, `TABULE_SIZE=4`).
- **Line Length**: Code lines **160 characters** (`RIGHT_MARGIN=160`), comment lines **90 characters** (`COMMENT_LINE_LENGTH=90`).
- **Blank Lines**:
  - Min **1 blank line** between declarations (`KEEP_BLANK_LINES_IN_DECLARATIONS=1`) and in code (`KEEP_BLANK_LINES_IN_CODE=1`).
  - **1 blank line** before closing brace (`KEEP_BLANK_LINES_BEFORE_RBRACE=1`).
  - **1 blank line** between package declaration and file header.
  - **1 blank line** between import groups and type declarations.
- **Imports**:
  - No wildcards (`.*`).
  - Order (with empty line between each section): Static imports (`*` packages) → `java.*` → `javax.*` → `jakarta.*` → `org.*` → `com.*` → External non-static imports → `xyz.opcal.*`.
- **Wrapping**:
  - Binary, additive, multiplicative, shift, bitwise, logical, relational, conditional, string concatenation, and assertion message operators: wrap.
  - Binary operation signs on the **next line**.
  - Method call chains, extends list, throws list, method/constructor parameter lists: wrap.
  - Do **not** keep control statements in one line.
  - Join wrapped lines is **disabled**.
- **Braces**:
  - Braces for blocks, switch cases, method/constructor/enum/record/annotation type declarations, lambdas: **end of line**.
  - `[{]` and `}]` of array initializers on same line as the array declaration.
- **Spacing**:
  - **No spaces** inside parentheses, brackets, or after `!`.
  - **No spaces** before/after `++`/`--`, prefix/postfix operators.
  - **No spaces** between method name and `(` in invocations.
  - **Spaces** around `=`, `+`, `-`, `*`, `/`, `%`, `&`, `|`, `^`, `<`, `>`, `==`, `!=`, `&&`, `||`, `?`, `:`, `->`.
- **JavaDocs**:
  - `@param` / `@throws` descriptions on a **new line**.
  - Do **not** wrap one-line comments.
  - Indent continuation lines.
- **Switch**: Case body aligned with switch statement.
- **Compact Statements**:
  - **No** `if-then` on one line.
  - **No** `if-else` on one line.
  - **No** enum/constants/annotations/records on one line.
  - Keep switch cases spread across multiple lines.
  - Empty array initializers on multiple lines.
  - Getters/setters on multiple lines.

## Known Issues

- No local services required; all external API calls go to `https://randomuser.me/api`.
