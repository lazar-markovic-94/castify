# CI/CD Documentation

This document describes the CI/CD pipelines and automation workflows for the Castify project.

## Overview

The project uses GitHub Actions for continuous integration and deployment with the following workflows:

1. **CI (Continuous Integration)** - Runs tests on every push and PR
2. **PR Checks** - Additional validation for pull requests
3. **CD (Continuous Deployment)** - Deploys to develop/production environments
4. **Security Scan** - Daily security vulnerability scans
5. **Dependabot** - Automated dependency updates

---

## Workflows

### 1. CI - Build and Test (`ci.yml`)

**Trigger:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches

**Jobs:**
- **Test** - Runs all unit tests with PostgreSQL
- **Code Quality** - Analyzes code quality
- **Build** - Builds the application JAR
- **Security Scan** - Scans for vulnerabilities
- **Summary** - Aggregates all results

**Requirements:**
- PostgreSQL 15 service container
- JDK 21
- Maven

**What it does:**
```bash
1. Sets up PostgreSQL test database
2. Runs mvn clean test with local profile
3. Generates test reports
4. Uploads test artifacts
5. Builds application JAR
6. Runs security scans
```

**Test Database:**
- Database: `castify_test`
- User: `castify_test`
- Password: `test_password`
- Port: 5432

---

### 2. PR Checks (`pr-checks.yml`)

**Trigger:**
- Pull request opened, synchronized, or reopened
- Only runs on non-draft PRs

**Jobs:**
- **PR Validation** - Validates PR title format (conventional commits)
- **Test Coverage** - Generates test coverage report with JaCoCo
- **Dependency Check** - Scans dependencies for vulnerabilities
- **Lint Dockerfile** - Lints Dockerfile if present
- **PR Comment** - Posts results as comment on PR

**Conventional Commit Types:**
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `style:` - Code style changes
- `refactor:` - Code refactoring
- `perf:` - Performance improvements
- `test:` - Test changes
- `build:` - Build system changes
- `ci:` - CI/CD changes
- `chore:` - Other changes

**Example PR Titles:**
```
✅ feat: add user authentication
✅ fix: resolve database connection timeout
✅ docs: update API documentation
❌ Added new feature (missing type prefix)
```

---

### 3. CD - Deploy (`cd.yml`)

**Trigger:**
- Push to `main` branch → Production deployment
- Push to `develop` branch → Development deployment
- Manual dispatch with environment selection

**Jobs:**

#### Deploy to Development
- Runs on `develop` branch pushes
- Spring Profile: `develop`
- Target: Development server
- Swagger: Enabled
- Tests: Runs before deployment

#### Deploy to Production
- Runs on `main` branch pushes
- Spring Profile: `production`
- Target: Production server
- Swagger: Disabled
- Tests: **Always runs** before production deployment
- Database Migrations: Runs Flyway migrations
- Health Checks: Verifies application health
- Smoke Tests: Basic functionality tests
- Rollback: Automatic on failure

**Manual Deployment:**
```bash
# Via GitHub UI:
Actions → CD - Deploy → Run workflow → Select environment
```

**Environment Protection:**
- Development: No restrictions
- Production: Requires approval (configure in GitHub Settings)

**Required Secrets:**
- `PROD_DB_USER` - Production database username
- `PROD_DB_PASSWORD` - Production database password
- `DEV_DB_USER` - Development database username
- `DEV_DB_PASSWORD` - Development database password

---

### 4. Security Scan (`security-scan.yml`)

**Trigger:**
- Daily at 2 AM UTC (scheduled)
- Push to `main` branch
- Manual dispatch

**Jobs:**
- **Dependency Security** - OWASP Dependency Check
- **Trivy Scan** - Container and filesystem vulnerability scan
- **Secret Scan** - Gitleaks for leaked secrets
- **CodeQL Analysis** - Static application security testing (SAST)

**OWASP Dependency Check:**
- Fails build on CVSS score >= 7
- Generates HTML report
- Uploads to artifacts

**Trivy Scan:**
- Scans for CRITICAL and HIGH severity vulnerabilities
- Uploads results to GitHub Security tab
- Ignores unfixed vulnerabilities

**Secret Scanning:**
- Scans entire git history
- Detects API keys, passwords, tokens
- Fails if secrets found

**CodeQL:**
- Analyzes Java/Kotlin code
- Detects security vulnerabilities
- Results in Security tab

**Automatic Issue Creation:**
- Creates GitHub issue if vulnerabilities found
- Labels: `security`, `high-priority`

---

### 5. Dependabot (`dependabot.yml`)

**Schedule:**
- Weekly on Monday at 8:00 AM

**Ecosystems:**
- **Maven** - Java/Kotlin dependencies
- **GitHub Actions** - Workflow dependencies
- **Docker** - Container image updates

**Grouping:**
Dependencies are grouped for easier review:
- Spring Framework updates together
- Kotlin updates together
- Testing libraries together

**Configuration:**
- Maximum 10 Maven PRs open at once
- Maximum 5 GitHub Actions PRs
- Automatic labels: `dependencies`, `automated`
- Commit message prefix based on type

---

## GitHub Secrets Required

Configure these in: **Settings → Secrets and variables → Actions**

### Required for CI
- `JWT_SECRET` (optional, has default) - JWT signing secret for tests

### Required for CD - Development
- `DEV_DB_URL` - Development database JDBC URL
- `DEV_DB_USER` - Development database username
- `DEV_DB_PASSWORD` - Development database password
- `DEV_JWT_SECRET` - Development JWT secret

### Required for CD - Production
- `PROD_DB_URL` - Production database JDBC URL
- `PROD_DB_USER` - Production database username
- `PROD_DB_PASSWORD` - Production database password
- `PROD_JWT_SECRET` - Production JWT secret (must be unique)

### Optional
- `CODECOV_TOKEN` - For code coverage reports
- `GITLEAKS_LICENSE` - For enhanced secret scanning
- `SLACK_WEBHOOK` - For deployment notifications

---

## Branch Protection Rules

**Recommended settings for `main` and `develop` branches:**

1. **Require pull request reviews**
   - At least 1 approval required

2. **Require status checks to pass**
   - CI - Build and Test
   - PR Checks - Test Coverage
   - PR Checks - Dependency Check

3. **Require branches to be up to date**
   - Ensures latest code is tested

4. **Do not allow force pushes**

5. **Require signed commits** (optional)

**Configure in:** Settings → Branches → Add rule

---

## Test Coverage

**JaCoCo Maven Plugin:**
- Minimum coverage: 50%
- Reports generated in `target/site/jacoco/`
- Coverage uploaded to Codecov

**Run locally:**
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

**Coverage check:**
```bash
mvn verify
```

---

## Workflow Status Badges

Add to README.md:

```markdown
[![CI](https://github.com/your-username/castify/actions/workflows/ci.yml/badge.svg)](https://github.com/your-username/castify/actions/workflows/ci.yml)
[![Security Scan](https://github.com/your-username/castify/actions/workflows/security-scan.yml/badge.svg)](https://github.com/your-username/castify/actions/workflows/security-scan.yml)
[![codecov](https://codecov.io/gh/your-username/castify/branch/main/graph/badge.svg)](https://codecov.io/gh/your-username/castify)
```

---

## Deployment Process

### Development Deployment (Automatic)

1. Merge PR to `develop` branch
2. CI workflow runs tests
3. CD workflow deploys to dev server
4. Application restarts with `develop` profile
5. Health check verifies deployment

### Production Deployment (Automatic)

1. Create PR from `develop` to `main`
2. PR checks run
3. Get approval
4. Merge PR to `main`
5. CI workflow runs tests
6. CD workflow:
   - Runs tests again
   - Builds production JAR
   - Runs database migrations
   - Deploys to production
   - Health checks
   - Smoke tests
7. Creates release tag
8. Rollback automatically if failure

### Manual Deployment

```bash
# Via GitHub UI
Actions → CD - Deploy → Run workflow → Select:
- Branch: main or develop
- Environment: develop or production
→ Run workflow
```

---

## Monitoring and Alerts

### Application Health
- Endpoint: `/actuator/health`
- Checked after every deployment

### Metrics
- Endpoint: `/actuator/metrics`
- Prometheus format: `/actuator/prometheus`

### Security Findings
- GitHub Security tab
- Dependabot alerts
- CodeQL results

---

## Troubleshooting

### Tests Failing in CI but Pass Locally

**Check:**
1. Database connection in CI uses PostgreSQL 15
2. Environment variables are properly set
3. Time zones (CI uses UTC)
4. File paths (CI uses Linux)

**Debug:**
```bash
# Run with CI profile locally
export SPRING_PROFILES_ACTIVE=local
export DB_URL=jdbc:postgresql://localhost:5432/castify_test
mvn clean test
```

### Deployment Failed

**Check:**
1. Workflow logs in Actions tab
2. Server logs on target machine
3. Database connectivity
4. Environment variables

**Rollback:**
- Automatic on failure
- Manual: Revert the merge commit and push

### Security Scan Issues

**False Positives:**
Create `dependency-check-suppressions.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
    <suppress>
        <notes>False positive for XYZ</notes>
        <cve>CVE-YYYY-NNNNN</cve>
    </suppress>
</suppressions>
```

---

## Best Practices

1. **Always run tests locally before pushing**
   ```bash
   mvn clean test
   ```

2. **Keep PRs small and focused**
   - Easier to review
   - Faster CI runs

3. **Write meaningful commit messages**
   - Follow conventional commits
   - Be descriptive

4. **Update tests with code changes**
   - Maintain test coverage
   - Update broken tests

5. **Review Dependabot PRs regularly**
   - Keep dependencies updated
   - Address security vulnerabilities

6. **Monitor Security tab**
   - Review vulnerability alerts
   - Fix critical issues promptly

7. **Test deployment process**
   - Use manual workflow dispatch
   - Verify in development first

---

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Spring Profiles Guide](./castify-app/PROFILES.md)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/)

---

## Support

For CI/CD issues:
1. Check workflow logs in Actions tab
2. Review this documentation
3. Check GitHub Actions status page
4. Create an issue with the `ci/cd` label
