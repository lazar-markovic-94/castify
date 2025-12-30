# 📧 Email Configuration Guide for Castify

This guide explains how to configure email for password reset functionality in different environments.

---

## 🏠 Local Development Setup

### Step 1: Copy the Template File
```bash
cd castify-app/src/main/resources/
cp application-local.yml.example application-local.yml
```

### Step 2: Get Gmail App Password

1. **Enable 2-Factor Authentication** (if not already enabled):
   - Go to: https://myaccount.google.com/security
   - Click "2-Step Verification"
   - Follow the prompts

2. **Generate App Password**:
   - Go to: https://myaccount.google.com/apppasswords
   - Sign in again if prompted
   - Select: **App** = "Mail", **Device** = "Other (Custom name)"
   - Type: "Castify Backend"
   - Click **Generate**
   - Copy the 16-character password (e.g., `abcd efgh ijkl mnop`)

### Step 3: Edit application-local.yml

Open `application-local.yml` and replace:

```yaml
spring:
  mail:
    username: your.email@gmail.com      # Your Gmail address
    password: abcdefghijklmnop          # Your app password (remove spaces)
```

### Step 4: Run Locally
```bash
# From the project root
mvn spring-boot:run -pl castify-app
```

**IMPORTANT:** `application-local.yml` is now in `.gitignore` and will NOT be committed to Git.

---

## 🚀 Production Setup (Hetzner Server)

### Option 1: Using Environment Variables (Recommended)

Set these environment variables on your Hetzner server:

```bash
# Email SMTP Configuration
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your.email@gmail.com
export MAIL_PASSWORD=your-app-password-here

# Application Email Settings
export MAIL_FROM=noreply@castify.com
export MAIL_FROM_NAME="Castify Team"
export FRONTEND_URL=https://castify.com
```

#### How to Set Permanently on Linux:

**Option A: Using systemd service file** (Recommended for production)

Create/edit your systemd service file:
```bash
sudo nano /etc/systemd/system/castify.service
```

Add environment variables:
```ini
[Unit]
Description=Castify Application
After=network.target

[Service]
Type=simple
User=castify
WorkingDirectory=/opt/castify
ExecStart=/usr/bin/java -jar castify-app.jar
Restart=on-failure

# Environment Variables
Environment="MAIL_HOST=smtp.gmail.com"
Environment="MAIL_PORT=587"
Environment="MAIL_USERNAME=your.email@gmail.com"
Environment="MAIL_PASSWORD=your-app-password"
Environment="MAIL_FROM=noreply@castify.com"
Environment="MAIL_FROM_NAME=Castify Team"
Environment="FRONTEND_URL=https://castify.com"

[Install]
WantedBy=multi-user.target
```

Then reload and restart:
```bash
sudo systemctl daemon-reload
sudo systemctl restart castify
```

**Option B: Using .env file** (for Docker or manual deployment)

Create `/opt/castify/.env`:
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your.email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@castify.com
MAIL_FROM_NAME=Castify Team
FRONTEND_URL=https://castify.com
```

Then source it before running:
```bash
cd /opt/castify
source .env
java -jar castify-app.jar
```

---

### Option 2: Using application-develop.yml or application-production.yml

Edit the appropriate profile file and add:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your.email@gmail.com
    password: your-app-password

app:
  mail:
    from: noreply@castify.com
    from-name: Castify Team
  frontend-url: https://castify.com
```

**WARNING:** Only use this if your deployment pipeline keeps credentials secure!

---

## 🔐 Security Best Practices

### ✅ DO:
- Use environment variables for production
- Use Gmail app passwords (not your real password)
- Create a dedicated email for Castify (e.g., `noreply.castify@gmail.com`)
- Keep `application-local.yml` in `.gitignore`
- Use different credentials for dev/staging/production

### ❌ DON'T:
- Commit credentials to Git
- Use your personal Gmail password
- Share app passwords
- Hardcode credentials in code
- Use the same email for dev and production

---

## 📬 Email Service Options

### Current Setup: Gmail SMTP (Free)
- ✅ Free up to 500 emails/day
- ✅ Easy to set up
- ✅ Reliable
- ⚠️ Daily limit may not be enough for large scale

### Alternative Options for Production:

#### 1. **SendGrid** (Recommended for Production)
- Free tier: 100 emails/day
- Paid: $19.95/month for 50,000 emails
- Professional email tracking and analytics
- Better deliverability

#### 2. **AWS SES**
- Very cheap: $0.10 per 1,000 emails
- Requires AWS account
- Excellent for high volume

#### 3. **Mailgun**
- Free tier: 5,000 emails/month
- Good developer experience
- EU servers available

---

## 🧪 Testing Email Functionality

### 1. Start the Application
```bash
mvn spring-boot:run -pl castify-app
```

### 2. Request Password Reset
```bash
curl -X POST http://localhost:8080/api/auth/password-reset/request \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

### 3. Check Your Email Inbox
You should receive a professional HTML email with a reset link.

### 4. Reset Password
```bash
curl -X POST http://localhost:8080/api/auth/password-reset/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "token": "TOKEN_FROM_EMAIL",
    "newPassword": "newPassword123"
  }'
```

---

## 🐛 Troubleshooting

### Error: "Authentication failed"
- **Cause:** Wrong username/password or 2FA not enabled
- **Solution:** Generate a new app password and ensure 2FA is enabled

### Error: "Connection timeout"
- **Cause:** Firewall blocking SMTP port 587
- **Solution:** Check firewall rules, try port 465 (SSL)

### Error: "Email not received"
- **Cause:** Check spam folder, wrong email address
- **Solution:** Check application logs for errors

### Emails go to spam
- **Solution:**
  - Use a dedicated domain email (not Gmail)
  - Set up SPF/DKIM records
  - Consider using SendGrid or AWS SES

### Check if email is configured correctly
```bash
# Check application logs
tail -f logs/castify-local.log | grep -i mail
```

---

## 📊 Monitoring

### Check Email Sending Status
All email operations are logged:
- ✅ Success: `Password reset email sent successfully to: user@example.com`
- ❌ Failure: `Failed to send password reset email to: user@example.com`

### Fallback Behavior
If email sending fails, the reset token is logged to console for testing:
```
Email sending failed. Reset token for user@example.com: AbCd1234EfGh5678...
```

---

## 🔄 Switching Email Providers

The application uses the `EmailService` interface. To switch providers:

1. Keep `SmtpEmailService` for SMTP-based providers (Gmail, Outlook, custom)
2. For SendGrid/AWS SES, create a new implementation
3. Mark it with `@Primary` annotation

All use cases will automatically use the new implementation!

---

## 📞 Support

For issues with email setup:
1. Check logs: `logs/castify-local.log` or server logs
2. Verify credentials are correct
3. Test SMTP connection manually
4. Check Gmail security settings

---

## 🎯 Quick Reference

| Environment | Configuration Method | Credentials Storage |
|-------------|---------------------|---------------------|
| **Local Dev** | `application-local.yml` | Local file (gitignored) |
| **Hetzner Server** | Environment variables | Systemd service file or .env |
| **Docker** | Environment variables | docker-compose.yml or .env |
| **Kubernetes** | Secrets | k8s secrets |

---

**Last Updated:** 2025-12-30
