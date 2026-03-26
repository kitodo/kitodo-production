# Security Policy

## 1. Purpose and scope

This policy defines how **KITODO e.V.** (GitHub orgnanisation: [https://github.com/kitodo](https://github.com/kitodo)) fulfils its obligations as an open‑source software steward under the EU **Cyber Resilience Act (CRA)** for all open‑source projects we systematically support on GitHub.

It applies to:

- GitHub repositories:
    - [KITODO-Presentation](https://github.com/kitodo/kitodo-presentation)
    - [KITODO-Production](https://github.com/kitodo/kitodo-production)
- All maintainers, core contributors, and staff acting on behalf of **KITODO e.V.**
- All infrastructure we provide for development (e.g. GitHub repos, GitHub Actions)

Hint: The other repositories under [KITODO](https://github.com/kitodo) are not subject to this regulation, as they are community driven, i.e. not actively maintainted by **KITODO e.V.**.

---

## 2. Roles and responsibilities

- **CRA Steward Lead**  
    - Overall owner of this policy; ensures CRA compliance.
    - Fulfilled by @kitodo/kitodo-board.

- **Security Contact**  
    - Handles vulnerability intake, coordination, and disclosure.  
    - Public contact in this [Security Policy](https://github.com/kitodo/kitodo-production/security/policy) (see [below](#41-reporting-channels-intake)).

- **Maintainers**  
    - Implement secure development practices.  
    - Fix vulnerabilities and publish releases.
    - GitHub team: @kitodo/kitodo-release-management

- **DevOps / Infrastructure**  
    - Maintain CI/CD.  
    - Respond to security incidents affecting development infrastructure.
    - GitHub team: @kitodo/kitodo-release-management

---

## 3. Secure development practices

For all in‑scope repositories:

- **Repository configuration**
    - Default branch protection enabled:
        - Require pull requests (no direct commits to default branch).
        - Require at least one approving review.
        - Require status checks to pass (CI workflows).
    - Security tab:
        - Enabled Dependabot alerts and Dependabot security updates.
        - Enabled secret scanning alerts (where available).
    - Branching and pull requests
        - All changes must go through pull requests (PRs).

- **CI / GitHub Actions**
    - Use GitHub Actions workflows to:
        - Run tests and linters on every PR and push to default branch.
        - Run SAST / code‑scanning where applicable (e.g. CodeQL).
    - Treat failed security checks as blocking for merges.

- **Dependency and artifact management**
    - Use Dependabot for dependency update PRs.
    - For container images or packages, store build configs in repo and, where possible, use reproducible builds.
    - Document supported versions and end‑of‑support dates in dedicated `SUPPORTED_VERSIONS.md`.

---

## 4. Vulnerability intake and handling

### 4.1 Reporting channels (intake)

We provide and maintain:

- **Public "Security" contact**
    - `security@kitodo.org`

- **`SECURITY.md`** (= this [Security Policy](https://github.com/kitodo/kitodo-production/security/policy) )

    Link to `SECURITY.md` (= this [Security Policy](https://github.com/kitodo/kitodo-production/security/policy)) from the repository root.

    Each in‑scope repo must contain a `SECURITY.md` (= this [Security Policy](https://github.com/kitodo/kitodo-production/security/policy) ) including:

    - How to report vulnerabilities.
    - Expected acknowledgement time.
    - Supported versions and branches - defined in [SUPPORTED_VERSIONS.md](SUPPORTED_VERSIONS.md)
    - Reference to our CRA steward role and potential reporting to EU authorities where legally required.

### 4.2 Triage and classification

Upon receiving a vulnerability report:

- **Acknowledgement**
    - Try to acknowledge the receipt within 5 working days via the channel it came from.
    - If the input data is not plausible, an acknowledgement might not be sent.

- **Confidentiality**
    - All vulnerabilities are handled confidentially until disclosure date, if not otherwise already published.

- **Severity assessment**
    - Classify severity (Low / Medium / High / Critical).
    - Decide whether it is likely to become / or already is actively exploited.

- **Relevance to CRA**
    - Determine if this vulnerability affects:
        - a product in our scope (see [1. Purpose and scope](#1-purpose-and-scope))
        - users in the EU
    - If yes, CRA vulnerability‑reporting duties (see [6. CRA‑specific reporting](61-triggers-for-vulnerability-reporting-art-141-via-243)) may apply.

### 4.3 Remediation and releases

- For each supported branch:
    - Create a fixing PR labeled `security`.
    - Ensure tests and security checks pass.
- After merging:
    - Create a release on GitHub (Releases tab) with:
        - Version number,
        - Short description of the security fix,
- For critical issues, consider:
    - temporary mitigations,
    - configuration workarounds,
    - guidance to disable vulnerable functionality.

### 4.4 Disclosure

- Notify users via:
    - GitHub release notes.
    - mailing lists.

---

## 5. Security incident management (development infrastructure)

A security incident in the CRA sense may involve:

- Compromise of GitHub org, repositories, GitHub Actions secrets, or package registry.
- Malicious code injection into default branch, releases, or GitHub Actions workflows.
- Account takeovers of maintainers with write or admin access.

General measures:

- Maintainers must have 2nd factor enabled.

### 5.1 Detection

- Monitor:
    - unusual pushes,
    - unexpected CI workflow modifications,
    - unexpected changes to repository or org settings.

### 5.2 Initial response

Try to react as fast as resonable possible:

- **Contain:**
    - Temporarily restrict repository access if needed.
    - Rotate GitHub Secrets and any compromised credentials.
    - Disable suspicious GitHub Actions workflows.

- **Record:**
    - Create an internal private incident issue.
    - Capture timeline, affected components, suspected cause, and current status.

- **Coordinate:**
    - Incident management is led by @kitodo/kitodo-release-management in resposibility of @kitodo/kitodo-board.

---

## 6. CRA‑specific reporting (Articles 14 & 24)

> Note: Timing and exact procedures depend on the official CRA implementation and the Single Reporting Platform (SRP) provided by EU/Member States. This policy describes our internal triggers and responsibilities.

### 6.1 Triggers for vulnerability reporting (Art. 14(1) via 24(3))

We consider CRA reporting when the following was determined:

- The vulnerability is in a product in our scope (see [1. Purpose and scope](#1-purpose-and-scope)); and
- It is actively exploited or there is credible evidence of exploitation in the wild; and
- Exploitation can lead to a severe impact on the security of products with digital elements used in the EU.

If triggered:

- CRA Steward Lead coordinates to prepare
    - current status to the authorities, when reasonable possible.
    - Final report after a fix is available.

### 6.2 Triggers for incident reporting (Art. 14(3) & (8) via 24(3))

We consider an incident CRA‑reportable if:

- It affects the security of our development infrastructure (e.g. GitHub org compromise, build system compromise)  
  **and** this can or has led to:
    - the introduction of malicious code, or
    - compromise of integrity or availability of our CRA‑relevant products; and
- The impact is severe for EU users.

If triggered:

- CRA Steward Lead coordinates to prepare
    - current status to the authorities, when reasonable possible.
    - Final report after a fix is available.

### 6.3 User / downstream notification

Whenever a CRA‑reportable vulnerability or incident is confirmed:

- Publish:
    - In case of a vulnerability: GitHub Releases with clear release notes,
    - On mailing list.
- Content must include:
    - description of the vulnerability/incident (as far as safely possible),
    - versions affected,
    - severity,
    - recommended actions or mitigations,
    - version(s) containing the fix (if available).

---

## 7. Cooperation with authorities

- **Single contact point:**  
  `security@kitodo.org` acts as the official contact point for market‑surveillance authorities.

- Upon a reasoned request from an EU authority, we will:
    - Provide a copy of this policy and related documentation (e.g. incident logs, vulnerability handling records) in a suitable language (e.g. English or German).
    - Cooperate in risk assessments and mitigation efforts related to our CRA‑relevant projects.

---

## 8. Documentation and retention

We keep, in GitHub repositories or internal systems:

- Records of vulnerabilities.
- Incident records and post‑mortems.
- Versions and change history of this policy.

Retention period: at least 5 years after the last relevant release of the associated product, unless a longer period is legally required.

---

## 9. Training and awareness

- New maintainers of CRA‑relevant repositories must complete a **security and CRA steward** onboarding, covering:
    - secure development on GitHub,
    - how to handle vulnerability reports,
    - when to escalate to @kitodo/kitodo-board.

This policy is reviewed at least **once per year** or after any major incident or CRA‑related guidance update.
