# CI/CD Quick Start Guide

Quick reference for common CI/CD tasks.

## For Developers

### Before Creating a PR

```bash
# 1. Run tests locally
mvn clean test

# 2. Check test coverage
mvn jacoco:report
open target/site/jacoco/index.html

# 3. Build the application
mvn clean package

# 4. Check for dependency vulnerabilities (optional)
mvn org.owasp:dependency-check-maven:check
```

### Creating a Pull Request

1. **Branch naming:**
   ```bash
   feature/add-user-auth
   fix/database-timeout
   docs/update-readme
   ```

2. **PR title format (Conventional Commits):**
   ```
   feat: add user authentication
   fix: resolve database connection timeout
   docs: update API documentation
   refactor: simplify rating logic
   test: add integration tests for auth
   ```

3. **What happens automatically:**
   - ✅ All tests run
   - ✅ Test coverage report generated
   - ✅ Security scan performed
   - ✅ Code quality checked
   - ✅ Results posted as PR comment

### Merging a PR

**To Develop:**
```bash
git checkout develop
git pull origin develop
git merge --no-ff feature/your-feature
git push origin develop
```
→ Automatically deploys to Development environment

**To Main (Production):**
1. Create PR from `develop` to `main`
2. Get approval from team
3. Merge PR
→ Automatically deploys to Production environment

---

## For Maintainers

### Manual Deployment

**Via GitHub UI:**
1. Go to **Actions** tab
2. Select **CD - Deploy** workflow
3. Click **Run workflow**
4. Choose environment: `develop` or `production`
5. Click **Run workflow**

### Checking Workflow Status

```bash
# All workflows
https://github.com/YOUR_USERNAME/castify/actions

# Specific workflow runs
Click on workflow → View details → Check logs
```

### Responding to Security Alerts

**When you get a security alert:**
1. Check **Security** tab
2. Review vulnerability details
3. Check if fix available
4. If critical:
   ```bash
   # Update dependency
   mvn versions:use-latest-versions
   # Or wait for Dependabot PR
   ```

### Managing Dependabot PRs

**Weekly Dependabot creates PRs for:**
- Maven dependencies
- GitHub Actions versions
- Docker images

**Review process:**
1. Check PR description
2. View changelog
3. Verify tests pass
4. Merge if safe

---

## Common Commands

### Run specific test
```bash
mvn test -Dtest=CreateRatingUseCaseTest
```

### Run tests with coverage
```bash
mvn clean test jacoco:report
```

### Build without tests
```bash
mvn clean package -DskipTests
```

### Run with specific profile
```bash
# Local
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Develop
mvn spring-boot:run -Dspring-boot.run.profiles=develop

# Production
java -jar target/castify-app.jar --spring.profiles.active=production
```

### Check for vulnerabilities
```bash
mvn org.owasp:dependency-check-maven:check
```

### Update dependencies
```bash
mvn versions:display-dependency-updates
```

---

## CI/CD Pipeline Flow

```
┌─────────────────────────────────────────────────────────────┐
│ Developer pushes code or creates PR                          │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│ CI Workflow (Automatic)                                      │
│ ✓ Build application                                          │
│ ✓ Run all tests                                              │
│ ✓ Generate test coverage                                     │
│ ✓ Security scan                                              │
│ ✓ Upload artifacts                                           │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│ PR Checks (On Pull Request)                                  │
│ ✓ Validate PR title                                          │
│ ✓ Check test coverage                                        │
│ ✓ Dependency security check                                  │
│ ✓ Post results as comment                                    │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│ Code Review & Approval                                       │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│ Merge to develop branch                                      │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│ CD Workflow - Deploy to Development (Automatic)              │
│ ✓ Build application                                          │
│ ✓ Deploy to dev server                                       │
│ ✓ Run health checks                                          │
└─────────────────────────────────────────────────────────────┘

                 OR

┌─────────────────────────────────────────────────────────────┐
│ Merge to main branch                                         │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│ CD Workflow - Deploy to Production (Automatic)               │
│ ✓ Run tests again                                            │
│ ✓ Build application                                          │
│ ✓ Run database migrations                                    │
│ ✓ Deploy to production                                       │
│ ✓ Health checks                                              │
│ ✓ Smoke tests                                                │
│ ✓ Create release tag                                         │
│ ⚠ Automatic rollback on failure                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Workflow Files Location

```
.github/
├── workflows/
│   ├── ci.yml              # Main CI pipeline
│   ├── cd.yml              # Deployment pipeline
│   ├── pr-checks.yml       # PR validation
│   └── security-scan.yml   # Security scanning
├── dependabot.yml          # Dependency updates
├── CI-CD.md               # Full documentation
└── QUICK-START.md         # This file
```

---

## Emergency Procedures

### Rollback Production Deployment

**Automatic:** Happens on deployment failure

**Manual:**
```bash
# Option 1: Revert merge commit
git revert -m 1 <merge-commit-hash>
git push origin main

# Option 2: Manual workflow dispatch
Actions → CD - Deploy → Run workflow → previous version
```

### Stop Deployment in Progress

1. Go to **Actions** tab
2. Find running workflow
3. Click **Cancel workflow**

### Disable Workflow Temporarily

1. Go to **Actions** tab
2. Select workflow
3. Click **⋮** (three dots)
4. Select **Disable workflow**

---

## Useful Links

- 📖 [Full CI/CD Documentation](./CI-CD.md)
- 🔧 [Spring Profiles Guide](../castify-app/PROFILES.md)
- 📊 [GitHub Actions](https://github.com/YOUR_USERNAME/castify/actions)
- 🔒 [Security Alerts](https://github.com/YOUR_USERNAME/castify/security)
- 📦 [Dependabot](https://github.com/YOUR_USERNAME/castify/security/dependabot)

---

## Getting Help

**Workflow failed?**
1. Check workflow logs in Actions tab
2. Look for red ❌ marks
3. Click to see detailed error

**Tests failing?**
```bash
# Run locally to debug
mvn test -Dtest=FailingTestName
```

**Need to bypass CI? (Emergency only)**
```bash
# Add [skip ci] to commit message
git commit -m "emergency fix [skip ci]"
```

**⚠️ Note:** Bypassing CI should be extremely rare and only for emergencies!
