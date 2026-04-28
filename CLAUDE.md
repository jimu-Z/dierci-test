# CLAUDE.md

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.
# CLAUDE 任务执行约束

## 计划文档路径
当前项目开发计划文档（正式执行）：
- F:/chuangye/启元农链_项目开发计划文档.md

当前项目开发计划文档设计方案（参考）：
- F:/chuangye/启元农链_项目开发计划文档_设计方案.md

## 根目录文档收敛
根目录只保留 5 份核心 Markdown：
- CLAUDE.md
- 启元农链_项目开发计划文档.md
- 启元农链_项目开发计划文档_设计方案.md
- 第三方平台分阶段接入执行手册.md
- EMQX_阶段A_联调配置模板.md

其余检查报告、优化清单、临时纪要类 Markdown 完成归档后应删除，避免根目录文档膨胀和口径分散。

## 执行规则
- 每次开启新任务前，必须先读取上述计划文档（至少读取“阶段总览、功能跟踪、风险台账”相关章节）。
- 每完成一个功能模块或功能点，必须在计划文档中同步更新：状态、完成度、实际完成时间、验收结论。
- 如出现延期、阻塞、范围变更，必须在计划文档中新增或更新“风险台账”和“变更台账”。
- 每周至少一次更新计划文档中的“阶段完成度”和“周报回顾”。

## 当前联调环境说明
- 当前没有稳定可复用的后端公网可访问地址时，第三方回调类联调优先使用临时公网隧道或本机验证，不要默认新对话已经具备固定公网后端。
- 涉及回调地址、白名单、外部平台联调时，若上下文里没有明确公网地址，先按临时地址或本地可验证路径继续推进，再等待人工补齐正式公网信息。
- 这条环境限制需要同步保留在新对话可见的根目录约束文档中，避免重复排查同一类前置条件。

## 文档维护规则
- 禁止仅口头说明进度，不更新计划文档。
- 若计划文档路径变更，需第一时间同步更新本文件。
- 本文件作为任务启动检查项，执行优先级高于临时说明。
