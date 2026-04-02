# Fixing On-Behalf-Of (OBO) Flow for Microsoft Graph

The `com.azure.core.exception.ClientAuthenticationException` during the On-Behalf-Of (OBO) flow is a classic sign of a misconfiguration in the Azure AD App Registration. Your code and `application.properties` are set up correctly, but the app registration itself is missing the necessary permissions to allow your backend to get a Graph token on behalf of your users.

Follow these steps precisely to resolve the issue.

---

### Step 1: Expose an API in Your App Registration

You must expose an API from your application, which allows other applications (or in this case, your own application) to request permissions.

1.  **Navigate to your App Registration:**
    *   Go to the [Azure Portal](https://portal.azure.com).
    *   Go to **Azure Active Directory**.
    *   Go to **App registrations**.
    *   Select the application you are using for this project.

2.  **Set the Application ID URI:**
    *   In the left-hand menu, click on **Expose an API**.
    *   At the top, click **Set** next to "Application ID URI".
    *   The default `api://<your-client-id>` is perfectly fine. Save it.

3.  **Add a Scope:**
    *   Click **+ Add a scope**.
    *   **Scope name:** Enter `user_impersonation` or `access_as_user`. This is a standard name for this type of scope.
    *   **Who can consent?:** Select **Admins and users**.
    *   **Admin consent display name:** Enter something descriptive, like `Access the API as the signed-in user`.
    *   **Admin consent description:** Enter `Allows the application to call the API with the same permissions as the signed-in user.`.
    *   **User consent display name (optional but recommended):** `Access the API as you`.
    *   **User consent description (optional but recommended):** `Allows the application to call its own backend API with your permissions.`.
    *   Ensure the state is **Enabled**.
    *   Click **Add scope**.

---

### Step 2: Grant Your App Permission to Its Own API

Now that you have exposed an API, your application needs permission to *use* that API.

1.  **Navigate to API Permissions:**
    *   In the left-hand menu, click on **API permissions**.

2.  **Add the New Permission:**
    *   Click **+ Add a permission**.
    *   Select the **My APIs** tab.
    *   You should see your application listed. Click on it.

3.  **Select the Exposed Scope:**
    *   Under "Permissions", check the box next to the scope you created in Step 1 (e.g., `user_impersonation`).
    *   Click **Add permissions**.

---

### Step 3: Grant Admin Consent

This is a critical step. The OBO flow requires admin consent for the permissions.

1.  **Stay on the API permissions page.**
2.  You should now see the `user_impersonation` (or `access_as_user`) permission in the list, along with your Microsoft Graph permissions (`Mail.Read`, `offline_access`, etc.).
3.  Click the **Grant admin consent for [Your Tenant Name]** button.
4.  A popup will appear. Click **Yes**.

After this, the "Status" column for all your permissions should show a green checkmark and say "Granted for [Your Tenant Name]".

---

### Summary of Required Permissions

After completing these steps, your **API permissions** page should have (at least) the following permissions, all with admin consent granted:

| API Name             | Permission Name       | Type       | Admin Consent Granted |
| -------------------- | --------------------- | ---------- | --------------------- |
| **Microsoft Graph**  | `Mail.Read`           | Delegated  | Yes                   |
| **Microsoft Graph**  | `offline_access`      | Delegated  | Yes                   |
| **Microsoft Graph**  | `openid`              | Delegated  | Yes                   |
| **Microsoft Graph**  | `profile`             | Delegated  | Yes                   |
| **[Your App Name]**  | `user_impersonation`  | Delegated  | Yes                   |

After you've completed these steps in the Azure Portal, restart your application and try the login flow again. The error should be resolved.
