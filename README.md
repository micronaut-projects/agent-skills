# Micronaut Agent Skills

A collection of Micronaut skills for AI coding agents. Skills are packaged instructions and scripts that extend agent capabilities.

Skills follow the [Agent Skills format](https://agentskills.io/specification).

## Installation

```
npx skills add micronaut-projects/agent-skills
```

## Evals

Each skill ships test cases in `skills/<skill>/evals/evals.json` following the
[Agent Skills evaluation guide](https://agentskills.io/skill-creation/evaluating-skills).
Run every case with and without the skill from a clean context and compare pass rates; see
`skills/micronaut-testing/evals/README.md` for the workspace layout.
