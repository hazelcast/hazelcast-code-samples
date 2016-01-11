<%-- Created by IntelliJ IDEA. User: bilal Date: 17/06/14 Time: 15:00
To change this template use File | Settings | File Templates. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" import="java.util.*"
        %>
<% session=request.getSession();%>
<html>

<head>
    <title>
    </title>
</head>

<body>
<div align="center">
    <table border="1">
        <tr>
            <td>
                Session Time Out Test
            </td>
            <td>
                <% out.println(session.getMaxInactiveInterval());%>
            </td>
        </tr>
        <tr>
            <td>
                Is New Test
            </td>
            <td>
                <% out.print(request.getAttribute( "isNewTest")); %>
            </td>
        </tr>
        <tr>
            <td>
                Session Creation Time
            </td>
            <td>
                <% out.print(new Date(session.getCreationTime())); %>
            </td>
        </tr>
        <tr>
            <td>
                Session Last Accessed Time
            </td>
            <td>
                <% out.println(new Date(session.getLastAccessedTime())); %>
            </td>
        </tr>
    </table>
    <form action="" method="get">
        <table>
            <tr>
                <td>
                    key:
                </td>
                <td>
                    <input type="text" name="key">
                </td>
            </tr>
            <tr>
                <td>
                    value:
                </td>
                <td>
                    <input type="text" name="value">
                </td>
            </tr>
            <tr>
                <td>
                    <input type="submit" name="action" value="Set Attribute">
                </td>
            </tr>
        </table>
    </form>
    <form action="" method="get" style="">
        <table>
            <tr>
                <td>
                    key:
                </td>
                <td>
                    <input type="text" name="key">
                </td>
            </tr>
            <tr>
                <td>
                    <input type="submit" name="action" value="Get Attribute" />
                </td>
                <td>
                    <input type="submit" name="action" value="Delete Attribute" />
                </td>
            </tr>
        </table>
    </form>
    <p>
        result:
        <%out.println(request.getAttribute( "getKey"));%>
    </p>
    <p>
        GetAttributeNames - Values:
        <% out.print(request.getAttribute( "res")); %>
    </p>
</div>
</body>

</html>
