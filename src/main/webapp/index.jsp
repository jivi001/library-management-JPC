<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:choose>
    <c:when test="${sessionScope.authenticated}">
        <c:redirect url="/dashboard" />
    </c:when>
    <c:otherwise>
        <c:redirect url="/login" />
    </c:otherwise>
</c:choose>
