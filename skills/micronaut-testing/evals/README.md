# Evals for `micronaut-testing`

Test cases follow the [Agent Skills evaluation guide](https://agentskills.io/skill-creation/evaluating-skills).

- `evals.json` — evals 1–7 cover the main pattern of each reference in `../references/`; evals
  8–25 cover the corner cases from each reference's notes. Each has a realistic prompt, the
  expected output, the input sources the prompt refers to, and assertions.
- `files/<name>/` — one directory per eval, matching its `name`, laid out as a project root
  (`src/main/...`). Copy it as the agent's working directory. These are inputs for the agent,
  not a buildable project.

The assertions deliberately target behaviour a model tends to get wrong *without* the skill
(`startApplication = false`, `spec.name` + `@Requires` scoping of stubs, Micronaut `JsonMapper`
instead of Jackson's `ObjectMapper`, `follow-redirects=false` for redirect tests, plain JUnit for
`BeanIntrospection`). Assertions that pass in both configurations should be replaced — they
don't measure the skill.

## Running an iteration

Run every eval twice from a clean context (a fresh subagent or session per run): once with the
skill loaded and once without. Save each run under a workspace outside the skill directory:

```
micronaut-testing-workspace/
└── iteration-1/
    ├── eval-1/
    │   ├── with_skill/    { outputs/, timing.json, grading.json }
    │   └── without_skill/ { outputs/, timing.json, grading.json }
    ├── eval-2/ …
    └── benchmark.json
```

Grade each assertion PASS/FAIL with quoted evidence from the output, then aggregate pass rates,
tokens and duration per configuration into `benchmark.json`. The `delta` between
`with_skill` and `without_skill` is the number that justifies (or not) keeping an instruction.

The `skill-creator` skill automates this loop.
