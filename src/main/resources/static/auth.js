const AUTH_MODE = {
    SIGN_IN: "signin",
    SIGN_UP: "signup"
};

const authForm = document.querySelector("#auth-form");
const authMessage = document.querySelector("#auth-message");
const modeToggle = document.querySelector("#mode-toggle");
const formTitle = document.querySelector("#form-title");
const submitButton = document.querySelector("#submit-button");
const nameField = document.querySelector("#name-field");
const passwordInput = document.querySelector("#password");
const emailInput = document.querySelector("#email");
const themeToggle = document.querySelector("#theme-toggle");
let mode = AUTH_MODE.SIGN_IN;

initTheme();
checkExistingSession();

modeToggle.addEventListener("click", () => {
    mode = mode === AUTH_MODE.SIGN_IN ? AUTH_MODE.SIGN_UP : AUTH_MODE.SIGN_IN;
    renderMode();
});

themeToggle.addEventListener("change", () => {
    setTheme(themeToggle.checked ? "dark" : "light");
});

authForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage("");

    const formData = new FormData(authForm);
    const payload = {
        email: String(formData.get("email") || "").trim(),
        password: String(formData.get("password") || "")
    };

    if (mode === AUTH_MODE.SIGN_UP) {
        payload.displayName = String(formData.get("displayName") || "").trim();
    }

    if (!payload.email || !payload.password) {
        setMessage("Email and password are required.", "error");
        return;
    }

    if (mode === AUTH_MODE.SIGN_UP && payload.password.length < 8) {
        setMessage("Use at least 8 characters for your password.", "error");
        return;
    }

    submitButton.disabled = true;

    try {
        await request(mode === AUTH_MODE.SIGN_IN ? "/api/auth/signin" : "/api/auth/signup", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        window.location.assign("/dashboard.html");
    } catch (error) {
        setMessage(error.message, "error");
    } finally {
        submitButton.disabled = false;
    }
});

document.querySelectorAll(".provider-button").forEach((button) => {
    button.addEventListener("click", async () => {
        const provider = button.dataset.provider;
        const email = emailInput.value.trim();

        if (!email) {
            setMessage("Enter your email before continuing with a provider.", "error");
            return;
        }

        button.disabled = true;
        try {
            await request(`/api/auth/provider/${provider}`, {
                method: "POST",
                body: JSON.stringify({email})
            });
        } catch (error) {
            setMessage(error.message, "error");
        } finally {
            button.disabled = false;
        }
    });
});

function renderMode() {
    const signingUp = mode === AUTH_MODE.SIGN_UP;
    nameField.hidden = !signingUp;
    formTitle.textContent = signingUp ? "Create account" : "Sign in";
    submitButton.textContent = signingUp ? "Create account" : "Sign in";
    modeToggle.textContent = signingUp ? "Already have an account? Sign in" : "Need an account? Sign up";
    passwordInput.autocomplete = signingUp ? "new-password" : "current-password";
    setMessage("");
}

async function checkExistingSession() {
    try {
        const response = await fetch("/api/auth/me", {credentials: "same-origin"});
        if (response.ok) {
            window.location.assign("/dashboard.html");
        }
    } catch (error) {
        setMessage("", "");
    }
}

async function request(url, options = {}) {
    const response = await fetch(url, {
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    if (!response.ok) {
        const payload = await safeJson(response);
        throw new Error(payload.message || "Unable to complete the request.");
    }

    return safeJson(response);
}

async function safeJson(response) {
    const text = await response.text();
    return text ? JSON.parse(text) : {};
}

function setMessage(message, type = "") {
    authMessage.textContent = message;
    authMessage.className = `auth-message ${type}`.trim();
}

function initTheme() {
    const storedTheme = localStorage.getItem("student-task-theme") || "light";
    setTheme(storedTheme);
}

function setTheme(theme) {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem("student-task-theme", theme);
    themeToggle.checked = theme === "dark";
}
