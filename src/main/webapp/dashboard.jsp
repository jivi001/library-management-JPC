<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard | JPC Library</title>
    <style>
        :root {
            --bg: #f4f7fb;
            --panel: #ffffff;
            --panel-alt: #f8fbff;
            --ink: #13212d;
            --muted: #6a7b8d;
            --border: #dbe5ee;
            --primary: #0f766e;
            --secondary: #1d4ed8;
            --accent: #f59e0b;
            --shadow: 0 16px 45px rgba(15, 23, 42, 0.08);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
            background:
                radial-gradient(circle at top right, rgba(15, 118, 110, 0.08), transparent 24%),
                linear-gradient(180deg, #f7fafc 0%, #eef4f8 100%);
            color: var(--ink);
        }

        .page {
            width: min(1180px, calc(100% - 32px));
            margin: 24px auto 40px;
        }

        .hero {
            background: linear-gradient(140deg, #123c4f 0%, #0f766e 62%, #2dd4bf 100%);
            color: #f8fafc;
            border-radius: 28px;
            padding: 34px 34px 28px;
            box-shadow: var(--shadow);
        }

        .topbar {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 18px;
            flex-wrap: wrap;
        }

        .eyebrow {
            font-size: 0.82rem;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            color: rgba(248, 250, 252, 0.78);
            margin-bottom: 10px;
        }

        .hero h1 {
            margin: 0;
            font-size: clamp(2rem, 4vw, 3rem);
            line-height: 1.08;
        }

        .hero p {
            margin: 10px 0 0;
            max-width: 620px;
            color: rgba(248, 250, 252, 0.85);
            line-height: 1.7;
        }

        .actions {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
        }

        .button,
        .button-ghost {
            appearance: none;
            border: none;
            border-radius: 14px;
            padding: 13px 18px;
            font-size: 0.95rem;
            font-weight: 700;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }

        .button {
            background: #ffffff;
            color: var(--primary);
        }

        .button-ghost {
            background: rgba(255, 255, 255, 0.12);
            color: #ffffff;
            border: 1px solid rgba(255, 255, 255, 0.18);
        }

        .metrics {
            display: grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 16px;
            margin-top: 24px;
        }

        .metric {
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.14);
            border-radius: 18px;
            padding: 18px;
        }

        .metric span {
            display: block;
            font-size: 0.9rem;
            color: rgba(248, 250, 252, 0.8);
            margin-bottom: 10px;
        }

        .metric strong {
            display: block;
            font-size: 2rem;
            line-height: 1;
        }

        .content {
            display: grid;
            grid-template-columns: 1.2fr 0.8fr;
            gap: 20px;
            margin-top: 22px;
        }

        .card {
            background: var(--panel);
            border: 1px solid var(--border);
            border-radius: 22px;
            padding: 24px;
            box-shadow: 0 10px 26px rgba(15, 23, 42, 0.04);
        }

        .card h2 {
            margin: 0 0 6px;
            font-size: 1.25rem;
        }

        .subtle {
            margin: 0 0 20px;
            color: var(--muted);
            line-height: 1.6;
        }

        .activity-list {
            display: grid;
            gap: 14px;
        }

        .activity-item {
            border: 1px solid var(--border);
            border-radius: 16px;
            padding: 16px;
            background: var(--panel-alt);
        }

        .activity-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
        }

        .activity-title {
            font-weight: 700;
            margin-bottom: 6px;
        }

        .activity-meta {
            color: var(--muted);
            font-size: 0.92rem;
        }

        .badge {
            display: inline-flex;
            align-items: center;
            padding: 7px 11px;
            border-radius: 999px;
            background: #e0f2fe;
            color: #0c4a6e;
            font-size: 0.82rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }

        .quick-links {
            display: grid;
            gap: 14px;
        }

        .link-card {
            border: 1px solid var(--border);
            border-radius: 18px;
            padding: 18px;
            background: linear-gradient(180deg, #ffffff 0%, #f9fbfd 100%);
        }

        .link-card strong {
            display: block;
            margin-bottom: 8px;
            font-size: 1rem;
        }

        .link-card p {
            margin: 0 0 14px;
            color: var(--muted);
            line-height: 1.55;
        }

        .link-card a {
            color: var(--secondary);
            text-decoration: none;
            font-weight: 700;
        }

        .empty-state {
            border: 1px dashed var(--border);
            border-radius: 18px;
            padding: 28px 20px;
            text-align: center;
            color: var(--muted);
            background: #fbfdff;
        }

        @media (max-width: 960px) {
            .metrics,
            .content {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
<c:url value="/books.jsp" var="booksPageUrl" />
<c:url value="/logout" var="logoutAction" />

<main class="page">
    <section class="hero">
        <div class="topbar">
            <div>
                <div class="eyebrow">JPC Library Dashboard</div>
                <h1>Welcome, <c:out value="${sessionScope.userName}" default="Reader" />.</h1>
                <p>
                    Track the current state of the library, review borrowing activity, and move quickly into book
                    management without losing context.
                </p>
            </div>

            <div class="actions">
                <a class="button" href="${booksPageUrl}">Open Books</a>
                <form action="${logoutAction}" method="post">
                    <button class="button-ghost" type="submit">Logout</button>
                </form>
            </div>
        </div>

        <div class="metrics">
            <div class="metric">
                <span>Total Books</span>
                <strong><c:out value="${requestScope.booksCount}" default="0" /></strong>
            </div>
            <div class="metric">
                <span>Available Copies</span>
                <strong><c:out value="${requestScope.availableBooks}" default="0" /></strong>
            </div>
            <div class="metric">
                <span>Active Loans</span>
                <strong><c:out value="${requestScope.activeLoans}" default="0" /></strong>
            </div>
            <div class="metric">
                <span>Pending Returns</span>
                <strong><c:out value="${requestScope.pendingReturns}" default="0" /></strong>
            </div>
        </div>
    </section>

    <section class="content">
        <article class="card">
            <h2>Recent Activity</h2>
            <p class="subtle">Use this area for the latest loan and return updates pushed in by the dashboard servlet.</p>

            <c:choose>
                <c:when test="${not empty requestScope.recentTransactions}">
                    <div class="activity-list">
                        <c:forEach var="transaction" items="${requestScope.recentTransactions}">
                            <div class="activity-item">
                                <div class="activity-row">
                                    <div>
                                        <div class="activity-title">Transaction #${transaction.id} for Book #${transaction.bookId}</div>
                                        <div class="activity-meta">
                                            Borrowed: ${transaction.borrowedAt}
                                            <c:if test="${not empty transaction.dueAt}">
                                                | Due: ${transaction.dueAt}
                                            </c:if>
                                        </div>
                                    </div>
                                    <span class="badge">${transaction.status}</span>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-state">
                        No recent activity has been loaded yet. Populate <code>recentTransactions</code> from the dashboard controller to show live updates here.
                    </div>
                </c:otherwise>
            </c:choose>
        </article>

        <aside class="card">
            <h2>Quick Actions</h2>
            <p class="subtle">Jump into the most common circulation tasks.</p>

            <div class="quick-links">
                <div class="link-card">
                    <strong>Manage books</strong>
                    <p>Add new titles, review current availability, and issue or return copies from one page.</p>
                    <a href="${booksPageUrl}">Go to books</a>
                </div>

                <div class="link-card">
                    <strong>Signed-in identity</strong>
                    <p>
                        Current session email:
                        <strong><c:out value="${sessionScope.userEmail}" default="Not available" /></strong>
                    </p>
                </div>

                <div class="link-card">
                    <strong>Next step</strong>
                    <p>Connect this page to a dedicated dashboard servlet and forward the latest stats as request attributes.</p>
                </div>
            </div>
        </aside>
    </section>
</main>
</body>
</html>
