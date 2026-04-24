<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Books | JPC Library</title>
    <style>
        :root {
            --bg: #f3f6fb;
            --panel: #ffffff;
            --soft: #f8fbff;
            --ink: #12202b;
            --muted: #66788a;
            --border: #d9e4ee;
            --primary: #0f766e;
            --secondary: #1d4ed8;
            --warn: #b45309;
            --shadow: 0 18px 48px rgba(15, 23, 42, 0.06);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
            color: var(--ink);
            background:
                radial-gradient(circle at top left, rgba(29, 78, 216, 0.08), transparent 22%),
                linear-gradient(180deg, #f7fafc 0%, #edf3f8 100%);
        }

        .page {
            width: min(1220px, calc(100% - 32px));
            margin: 22px auto 40px;
            display: grid;
            gap: 20px;
        }

        .hero {
            background: linear-gradient(140deg, #1f2937 0%, #1d4ed8 60%, #38bdf8 100%);
            color: #f8fafc;
            border-radius: 28px;
            padding: 30px 30px 26px;
            box-shadow: var(--shadow);
        }

        .hero-top {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 16px;
            flex-wrap: wrap;
        }

        .hero h1 {
            margin: 0 0 10px;
            font-size: clamp(2rem, 4vw, 2.8rem);
            line-height: 1.08;
        }

        .hero p {
            margin: 0;
            max-width: 720px;
            line-height: 1.7;
            color: rgba(248, 250, 252, 0.84);
        }

        .hero-actions {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
        }

        .hero-actions a {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 12px 16px;
            border-radius: 14px;
            text-decoration: none;
            font-weight: 700;
        }

        .solid-link {
            background: #ffffff;
            color: var(--secondary);
        }

        .ghost-link {
            background: rgba(255, 255, 255, 0.12);
            border: 1px solid rgba(255, 255, 255, 0.18);
            color: #ffffff;
        }

        .content {
            display: grid;
            grid-template-columns: 0.95fr 1.35fr;
            gap: 20px;
        }

        .stack {
            display: grid;
            gap: 20px;
        }

        .card {
            background: var(--panel);
            border: 1px solid var(--border);
            border-radius: 22px;
            padding: 24px;
            box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
        }

        .card h2 {
            margin: 0 0 8px;
            font-size: 1.2rem;
        }

        .subtle {
            margin: 0 0 18px;
            color: var(--muted);
            line-height: 1.6;
        }

        .alert {
            margin-bottom: 16px;
            padding: 14px 16px;
            border-radius: 14px;
            background: #eff6ff;
            color: #1d4ed8;
            border: 1px solid #bfdbfe;
            font-size: 0.94rem;
        }

        form {
            display: grid;
            gap: 14px;
        }

        .field,
        .field-grid {
            display: grid;
            gap: 8px;
        }

        .field-grid {
            grid-template-columns: 1fr 1fr;
        }

        label {
            font-size: 0.92rem;
            font-weight: 600;
            color: #243445;
        }

        input {
            width: 100%;
            padding: 13px 14px;
            border-radius: 14px;
            border: 1px solid var(--border);
            background: #fbfdff;
            font-size: 0.96rem;
            transition: border-color 0.2s ease, box-shadow 0.2s ease;
        }

        input:focus {
            outline: none;
            border-color: rgba(15, 118, 110, 0.55);
            box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.14);
        }

        .submit {
            border: none;
            border-radius: 14px;
            padding: 14px 16px;
            font-size: 0.95rem;
            font-weight: 700;
            cursor: pointer;
            color: #ffffff;
            background: linear-gradient(135deg, var(--primary) 0%, #14b8a6 100%);
        }

        .submit.secondary {
            background: linear-gradient(135deg, var(--secondary) 0%, #3b82f6 100%);
        }

        .submit.warn {
            background: linear-gradient(135deg, #c2410c 0%, #f59e0b 100%);
        }

        .table-wrap {
            overflow-x: auto;
            border: 1px solid var(--border);
            border-radius: 18px;
            background: var(--soft);
        }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 760px;
        }

        thead th {
            text-align: left;
            padding: 16px;
            font-size: 0.83rem;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            color: var(--muted);
            background: #f6fafe;
            border-bottom: 1px solid var(--border);
        }

        tbody td {
            padding: 16px;
            border-bottom: 1px solid var(--border);
            vertical-align: top;
        }

        tbody tr:last-child td {
            border-bottom: none;
        }

        .title-cell strong {
            display: block;
            margin-bottom: 4px;
            font-size: 0.98rem;
        }

        .title-cell span,
        .muted {
            color: var(--muted);
            font-size: 0.92rem;
        }

        .availability {
            display: inline-flex;
            align-items: center;
            padding: 7px 11px;
            border-radius: 999px;
            background: #dcfce7;
            color: #166534;
            font-weight: 700;
            font-size: 0.84rem;
        }

        .availability.low {
            background: #fef3c7;
            color: var(--warn);
        }

        .empty-state {
            padding: 36px 24px;
            text-align: center;
            color: var(--muted);
            border: 1px dashed var(--border);
            border-radius: 18px;
            background: #fbfdff;
        }

        @media (max-width: 1040px) {
            .content {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 720px) {
            .field-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
<c:url value="/books/add" var="addBookAction" />
<c:url value="/books/issue" var="issueBookAction" />
<c:url value="/books/return" var="returnBookAction" />
<c:url value="/dashboard" var="dashboardPageUrl" />
<c:url value="/logout" var="logoutAction" />

<main class="page">
    <section class="hero">
        <div class="hero-top">
            <div>
                <h1>Books and circulation</h1>
                <p>
                    Add new titles, review current availability, issue books to the signed-in user, and close return
                    transactions from the same workspace.
                </p>
            </div>

            <div class="hero-actions">
                <a class="solid-link" href="${dashboardPageUrl}">Dashboard</a>
                <form action="${logoutAction}" method="post">
                    <button class="ghost-link" type="submit">Logout</button>
                </form>
            </div>
        </div>
    </section>

    <c:if test="${not empty requestScope.message}">
        <div class="alert"><c:out value="${requestScope.message}" /></div>
    </c:if>
    <c:if test="${not empty requestScope.errorMessage}">
        <div class="alert" style="background: #fff1f2; color: #b42318; border-color: #fecdca;">
            <c:out value="${requestScope.errorMessage}" />
        </div>
    </c:if>

    <section class="content">
        <div class="stack">
            <article class="card">
                <h2>Add Book</h2>
                <p class="subtle">Register a new title and immediately set its initial available copies.</p>

                <form action="${addBookAction}" method="post" novalidate>
                    <div class="field">
                        <label for="title">Title</label>
                        <input id="title" name="title" type="text" maxlength="255" placeholder="Book title" required>
                    </div>

                    <div class="field">
                        <label for="author">Author</label>
                        <input id="author" name="author" type="text" maxlength="255" placeholder="Author name" required>
                    </div>

                    <div class="field-grid">
                        <div class="field">
                            <label for="isbn">ISBN</label>
                            <input id="isbn" name="isbn" type="text" maxlength="20" placeholder="Optional ISBN">
                        </div>
                        <div class="field">
                            <label for="publishedYear">Published Year</label>
                            <input id="publishedYear" name="publishedYear" type="number" min="1000" max="9999" placeholder="2026">
                        </div>
                    </div>

                    <div class="field-grid">
                        <div class="field">
                            <label for="publisher">Publisher</label>
                            <input id="publisher" name="publisher" type="text" maxlength="255" placeholder="Optional publisher">
                        </div>
                        <div class="field">
                            <label for="category">Category</label>
                            <input id="category" name="category" type="text" maxlength="100" placeholder="Optional category">
                        </div>
                    </div>

                    <div class="field-grid">
                        <div class="field">
                            <label for="totalCopies">Total Copies</label>
                            <input id="totalCopies" name="totalCopies" type="number" min="1" placeholder="1" required>
                        </div>
                        <div class="field">
                            <label for="shelfLocation">Shelf Location</label>
                            <input id="shelfLocation" name="shelfLocation" type="text" maxlength="50" placeholder="A-12">
                        </div>
                    </div>

                    <button class="submit" type="submit">Add Book</button>
                </form>
            </article>

            <article class="card">
                <h2>Issue Book</h2>
                <p class="subtle">Create a new borrowing transaction for the current session user.</p>

                <form action="${issueBookAction}" method="post" novalidate>
                    <div class="field">
                        <label for="bookId">Book ID</label>
                        <input id="bookId" name="bookId" type="number" min="1" placeholder="Enter book id" required>
                    </div>

                    <div class="field">
                        <label for="dueDate">Due Date</label>
                        <input id="dueDate" name="dueDate" type="date" required>
                    </div>

                    <div class="field">
                        <label for="issueNotes">Notes</label>
                        <input id="issueNotes" name="notes" type="text" maxlength="500" placeholder="Optional issuing note">
                    </div>

                    <button class="submit secondary" type="submit">Issue Book</button>
                </form>
            </article>

            <article class="card">
                <h2>Return Book</h2>
                <p class="subtle">Close an existing transaction and restore the book's available count.</p>

                <form action="${returnBookAction}" method="post" novalidate>
                    <div class="field">
                        <label for="transactionId">Transaction ID</label>
                        <input id="transactionId" name="transactionId" type="number" min="1" placeholder="Enter transaction id" required>
                    </div>

                    <div class="field">
                        <label for="fineAmount">Fine Amount</label>
                        <input id="fineAmount" name="fineAmount" type="number" min="0" step="0.01" placeholder="0.00">
                    </div>

                    <div class="field">
                        <label for="returnNotes">Notes</label>
                        <input id="returnNotes" name="notes" type="text" maxlength="500" placeholder="Optional return note">
                    </div>

                    <button class="submit warn" type="submit">Return Book</button>
                </form>
            </article>
        </div>

        <article class="card">
            <h2>Available Books</h2>
            <p class="subtle">This table expects a request attribute named <code>books</code> containing active book records.</p>

            <c:choose>
                <c:when test="${not empty requestScope.books}">
                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>Title</th>
                                <th>Author</th>
                                <th>ISBN</th>
                                <th>Category</th>
                                <th>Copies</th>
                                <th>Shelf</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="book" items="${requestScope.books}">
                                <tr>
                                    <td class="title-cell">
                                        <strong>${book.title}</strong>
                                        <span>ID #${book.id}</span>
                                    </td>
                                    <td>${book.author}</td>
                                    <td><c:out value="${book.isbn}" default="-" /></td>
                                    <td><c:out value="${book.category}" default="-" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${book.availableCopies le 1}">
                                                <span class="availability low">${book.availableCopies} of ${book.totalCopies}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="availability">${book.availableCopies} of ${book.totalCopies}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${book.shelfLocation}" default="-" /></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-state">
                        No books are loaded yet. Forward a <code>books</code> collection from a controller to render the inventory table.
                    </div>
                </c:otherwise>
            </c:choose>
        </article>
    </section>
</main>
</body>
</html>
