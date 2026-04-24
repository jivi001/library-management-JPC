<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login | JPC Library</title>
    <style>
        :root {
            --bg: #f4f7fb;
            --panel: #ffffff;
            --ink: #13212d;
            --muted: #61758a;
            --border: #d7e2ec;
            --primary: #1d4ed8;
            --primary-dark: #1d3fae;
            --secondary: #0f766e;
            --danger-bg: #fff1f2;
            --danger-text: #b42318;
            --success-bg: #ecfdf3;
            --success-text: #027a48;
            --shadow: 0 20px 50px rgba(15, 23, 42, 0.08);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
            color: var(--ink);
            background:
                radial-gradient(circle at left top, rgba(29, 78, 216, 0.16), transparent 30%),
                radial-gradient(circle at right bottom, rgba(15, 118, 110, 0.12), transparent 26%),
                linear-gradient(180deg, #f8fbff 0%, #eef4f8 100%);
            display: grid;
            place-items: center;
            padding: 20px;
        }

        .layout {
            width: min(980px, 100%);
            display: grid;
            grid-template-columns: 0.95fr 1.05fr;
            background: var(--panel);
            border: 1px solid rgba(215, 226, 236, 0.9);
            border-radius: 28px;
            overflow: hidden;
            box-shadow: var(--shadow);
        }

        .aside {
            background: linear-gradient(170deg, #1e293b 0%, #1d4ed8 56%, #38bdf8 100%);
            color: #f8fafc;
            padding: 46px 38px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            min-height: 620px;
        }

        .aside h1 {
            margin: 0 0 14px;
            font-size: clamp(2rem, 4vw, 3rem);
            line-height: 1.05;
        }

        .aside p {
            margin: 0;
            max-width: 360px;
            line-height: 1.7;
            color: rgba(248, 250, 252, 0.84);
        }

        .stats {
            display: grid;
            gap: 12px;
            margin-top: 30px;
        }

        .stat {
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.12);
            border-radius: 18px;
            padding: 16px 18px;
        }

        .stat strong {
            display: block;
            font-size: 1.1rem;
            margin-bottom: 4px;
        }

        .stat span {
            font-size: 0.92rem;
            color: rgba(248, 250, 252, 0.82);
        }

        .panel {
            padding: 52px 42px;
            display: flex;
            flex-direction: column;
            justify-content: center;
        }

        .panel h2 {
            margin: 0 0 10px;
            font-size: 1.9rem;
        }

        .lead {
            margin: 0 0 28px;
            color: var(--muted);
            line-height: 1.65;
        }

        .alert {
            padding: 14px 16px;
            border-radius: 14px;
            margin-bottom: 18px;
            font-size: 0.95rem;
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
            font-weight: 600;
            font-size: 0.92rem;
            color: #243445;
        }

        input {
            width: 100%;
            padding: 14px 15px;
            border-radius: 14px;
            border: 1px solid var(--border);
            background: #fbfdff;
            font-size: 0.98rem;
            transition: border-color 0.2s ease, box-shadow 0.2s ease;
        }

        input:focus {
            outline: none;
            border-color: rgba(29, 78, 216, 0.55);
            box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.14);
        }

        .actions {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 14px;
            margin-top: 8px;
        }

        .remember {
            color: var(--muted);
            font-size: 0.92rem;
        }

        .submit {
            border: none;
            border-radius: 14px;
            background: linear-gradient(135deg, var(--primary) 0%, #2563eb 100%);
            color: #fff;
            padding: 15px 18px;
            font-size: 0.98rem;
            font-weight: 700;
            cursor: pointer;
            width: 100%;
            margin-top: 6px;
        }

        .submit:hover {
            background: linear-gradient(135deg, var(--primary-dark) 0%, #1d4ed8 100%);
        }

        .bottom-link {
            margin-top: 22px;
            color: var(--muted);
            font-size: 0.94rem;
        }

        .bottom-link a {
            color: var(--secondary);
            font-weight: 700;
            text-decoration: none;
        }

        @media (max-width: 900px) {
            .layout {
                grid-template-columns: 1fr;
            }

            .aside,
            .panel {
                min-height: auto;
                padding: 32px 24px;
            }
        }
    </style>
</head>
<body>
<c:url value="/login" var="loginAction" />
<c:url value="/signup" var="signupPageUrl" />

<main class="layout">
    <aside class="aside">
        <div>
            <h1>Welcome back to your library workspace</h1>
            <p>
                Sign in to review active loans, browse book availability, and keep circulation moving without friction.
            </p>

            <div class="stats">
                <div class="stat">
                    <strong>Verified access</strong>
                    <span>Only confirmed accounts can enter the borrowing workflow.</span>
                </div>
                <div class="stat">
                    <strong>Session safety</strong>
                    <span>Built around server-side sessions for reliable authenticated access.</span>
                </div>
                <div class="stat">
                    <strong>Books and transactions</strong>
                    <span>Availability and borrow history stay in sync across the app.</span>
                </div>
            </div>
        </div>
    </aside>

    <section class="panel">
        <h2>Login</h2>
        <p class="lead">Enter your account credentials to continue into the dashboard.</p>

        <c:if test="${param.verified eq 'success'}">
            <div class="alert success">Your account has been verified. You can log in now.</div>
        </c:if>
        <c:if test="${param.verified eq 'invalid'}">
            <div class="alert error">The verification link is invalid or has expired.</div>
        </c:if>
        <c:if test="${param.signup eq 'success'}">
            <div class="alert success">Signup successful. Please check your email and verify your account.</div>
        </c:if>
        <c:if test="${param.logout eq 'success'}">
            <div class="alert success">You have been logged out successfully.</div>
        </c:if>
        <c:if test="${param.session eq 'expired'}">
            <div class="alert error">Your session expired. Please log in again.</div>
        </c:if>
        <c:if test="${not empty requestScope.errorMessage}">
            <div class="alert error">${requestScope.errorMessage}</div>
        </c:if>
        <c:if test="${not empty requestScope.successMessage}">
            <div class="alert success">${requestScope.successMessage}</div>
        </c:if>

        <form action="${loginAction}" method="post" novalidate>
            <div class="field">
                <label for="email">Email Address</label>
                <input id="email" name="email" type="email" maxlength="255"
                       value="${param.email}" placeholder="you@example.com" required>
            </div>

            <div class="field">
                <label for="password">Password</label>
                <input id="password" name="password" type="password" maxlength="72"
                       placeholder="Enter your password" required>
            </div>

            <div class="actions">
                <div class="remember">Verified users only</div>
            </div>

            <button class="submit" type="submit">Sign In</button>
        </form>

        <p class="bottom-link">
            Need an account?
            <a href="${signupPageUrl}">Create one here</a>
        </p>
    </section>
</main>
</body>
</html>
