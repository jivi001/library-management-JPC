<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign Up | JPC Library</title>
    <style>
        :root {
            --bg: #f3f7fb;
            --panel: #ffffff;
            --text: #16202a;
            --muted: #66788a;
            --border: #d8e1ea;
            --primary: #0f766e;
            --primary-dark: #115e59;
            --accent: #f59e0b;
            --danger-bg: #fff1f2;
            --danger-text: #b42318;
            --success-bg: #ecfdf3;
            --success-text: #027a48;
            --shadow: 0 24px 60px rgba(15, 23, 42, 0.08);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
            background:
                radial-gradient(circle at top right, rgba(15, 118, 110, 0.14), transparent 28%),
                linear-gradient(180deg, #f7fafc 0%, #eef4f9 100%);
            color: var(--text);
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 32px 16px;
        }

        .shell {
            width: min(1040px, 100%);
            display: grid;
            grid-template-columns: 1.05fr 0.95fr;
            background: var(--panel);
            border: 1px solid rgba(216, 225, 234, 0.8);
            border-radius: 28px;
            overflow: hidden;
            box-shadow: var(--shadow);
        }

        .hero {
            padding: 56px 48px;
            background: linear-gradient(160deg, #123c4f 0%, #0f766e 58%, #14b8a6 100%);
            color: #f8fafc;
            position: relative;
        }

        .hero::after {
            content: "";
            position: absolute;
            inset: auto -60px -80px auto;
            width: 220px;
            height: 220px;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.08);
        }

        .eyebrow {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            font-size: 0.82rem;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            color: rgba(248, 250, 252, 0.82);
            margin-bottom: 22px;
        }

        .hero h1 {
            margin: 0 0 14px;
            font-size: clamp(2rem, 4vw, 3.2rem);
            line-height: 1.05;
        }

        .hero p {
            margin: 0 0 30px;
            max-width: 420px;
            font-size: 1rem;
            line-height: 1.7;
            color: rgba(248, 250, 252, 0.86);
        }

        .feature-list {
            display: grid;
            gap: 14px;
            margin-top: 28px;
        }

        .feature {
            padding: 14px 16px;
            border: 1px solid rgba(255, 255, 255, 0.16);
            border-radius: 16px;
            background: rgba(255, 255, 255, 0.08);
            backdrop-filter: blur(10px);
        }

        .feature strong {
            display: block;
            margin-bottom: 4px;
            font-size: 0.96rem;
        }

        .feature span {
            font-size: 0.92rem;
            color: rgba(248, 250, 252, 0.8);
        }

        .panel {
            padding: 48px 40px;
            display: flex;
            flex-direction: column;
            justify-content: center;
        }

        .panel h2 {
            margin: 0 0 10px;
            font-size: 1.8rem;
        }

        .panel-copy {
            margin: 0 0 28px;
            color: var(--muted);
            line-height: 1.65;
        }

        .alert {
            margin-bottom: 18px;
            border-radius: 14px;
            padding: 14px 16px;
            font-size: 0.95rem;
            line-height: 1.5;
            border: 1px solid transparent;
        }

        .alert.error {
            background: var(--danger-bg);
            color: var(--danger-text);
            border-color: #fecdca;
        }

        .alert.success {
            background: var(--success-bg);
            color: var(--success-text);
            border-color: #abefc6;
        }

        form {
            display: grid;
            gap: 16px;
        }

        .field {
            display: grid;
            gap: 8px;
        }

        label {
            font-size: 0.92rem;
            font-weight: 600;
            color: #243445;
        }

        input {
            width: 100%;
            padding: 14px 15px;
            border-radius: 14px;
            border: 1px solid var(--border);
            background: #fbfdff;
            font-size: 0.98rem;
            color: var(--text);
            transition: border-color 0.2s ease, box-shadow 0.2s ease;
        }

        input:focus {
            outline: none;
            border-color: rgba(15, 118, 110, 0.65);
            box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.16);
        }

        .field-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
        }

        .submit {
            margin-top: 10px;
            border: none;
            border-radius: 14px;
            background: linear-gradient(135deg, var(--primary) 0%, #14b8a6 100%);
            color: #ffffff;
            padding: 15px 18px;
            font-size: 1rem;
            font-weight: 700;
            cursor: pointer;
        }

        .submit:hover {
            background: linear-gradient(135deg, var(--primary-dark) 0%, #0f766e 100%);
        }

        .footer-note {
            margin-top: 22px;
            color: var(--muted);
            font-size: 0.94rem;
        }

        .footer-note a {
            color: var(--primary);
            text-decoration: none;
            font-weight: 700;
        }

        @media (max-width: 900px) {
            .shell {
                grid-template-columns: 1fr;
            }

            .hero,
            .panel {
                padding: 34px 24px;
            }

            .field-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
<c:url value="/signup" var="signupAction" />
<c:url value="/login" var="loginPageUrl" />

<main class="shell">
    <section class="hero">
        <div class="eyebrow">JPC Library Access</div>
        <h1>Create your reader account</h1>
        <p>
            Start tracking borrowed books, manage your reading workflow, and keep your library activity in one quiet,
            organized place.
        </p>

        <div class="feature-list">
            <div class="feature">
                <strong>Email verification</strong>
                <span>Account access opens only after your email address is confirmed.</span>
            </div>
            <div class="feature">
                <strong>Borrowing visibility</strong>
                <span>Review availability, issue history, and due dates from a central dashboard.</span>
            </div>
            <div class="feature">
                <strong>Faster staff workflows</strong>
                <span>Designed for clean handoffs between user accounts, books, and transaction records.</span>
            </div>
        </div>
    </section>

    <section class="panel">
        <h2>Sign up</h2>
        <p class="panel-copy">Fill in your details to create a new account. A verification email will be sent right after registration.</p>

        <c:if test="${not empty requestScope.errorMessage}">
            <div class="alert error">${requestScope.errorMessage}</div>
        </c:if>
        <c:if test="${not empty requestScope.successMessage}">
            <div class="alert success">${requestScope.successMessage}</div>
        </c:if>

        <form action="${signupAction}" method="post" novalidate>
            <div class="field">
                <label for="fullName">Full Name</label>
                <input id="fullName" name="fullName" type="text" maxlength="120"
                       value="${param.fullName}" placeholder="Enter your full name" required>
            </div>

            <div class="field">
                <label for="email">Email Address</label>
                <input id="email" name="email" type="email" maxlength="255"
                       value="${param.email}" placeholder="you@example.com" required>
            </div>

            <div class="field-grid">
                <div class="field">
                    <label for="password">Password</label>
                    <input id="password" name="password" type="password" minlength="12" maxlength="72"
                           placeholder="Minimum 12 characters" required>
                </div>
                <div class="field">
                    <label for="confirmPassword">Confirm Password</label>
                    <input id="confirmPassword" name="confirmPassword" type="password" minlength="12" maxlength="72"
                           placeholder="Re-enter password" required>
                </div>
            </div>

            <button class="submit" type="submit">Create Account</button>
        </form>

        <p class="footer-note">
            Already registered?
            <a href="${loginPageUrl}">Go to login</a>
        </p>
    </section>
</main>
</body>
</html>
