const THEME_KEY = "student-task-theme";
const state = {
    user: null,
    tasks: [],
    settings: null,
    activeView: "all",
    editingTaskId: null,
    calendarMonth: new Date(new Date().getFullYear(), new Date().getMonth(), 1)
};

const elements = {
    themeToggle: document.querySelector("#theme-toggle"),
    logoutButton: document.querySelector("#logout-button"),
    welcomeLabel: document.querySelector("#welcome-label"),
    taskForm: document.querySelector("#task-form"),
    taskFormTitle: document.querySelector("#task-form-title"),
    taskSubmitButton: document.querySelector("#task-submit-button"),
    cancelEditButton: document.querySelector("#cancel-edit-button"),
    taskTitle: document.querySelector("#task-title"),
    taskDescription: document.querySelector("#task-description"),
    taskCategory: document.querySelector("#task-category"),
    taskPriority: document.querySelector("#task-priority"),
    taskDueDate: document.querySelector("#task-due-date"),
    taskList: document.querySelector("#task-list"),
    taskListTitle: document.querySelector("#task-list-title"),
    taskListKicker: document.querySelector("#task-list-kicker"),
    visibleCount: document.querySelector("#visible-count"),
    progressLabel: document.querySelector("#progress-label"),
    taskCountLabel: document.querySelector("#task-count-label"),
    progressFill: document.querySelector("#progress-fill"),
    tabs: document.querySelectorAll(".tab-button"),
    tasksView: document.querySelector("#tasks-view"),
    settingsView: document.querySelector("#settings-view"),
    settingsForm: document.querySelector("#settings-form"),
    digestDaily: document.querySelector("#digest-daily"),
    digestWeekly: document.querySelector("#digest-weekly"),
    examReminders: document.querySelector("#exam-reminders"),
    assignmentReminders: document.querySelector("#assignment-reminders"),
    timezoneSelect: document.querySelector("#timezone-select"),
    calendarTitle: document.querySelector("#calendar-title"),
    calendarGrid: document.querySelector("#calendar-grid"),
    previousMonth: document.querySelector("#previous-month"),
    nextMonth: document.querySelector("#next-month"),
    toast: document.querySelector("#toast")
};

document.addEventListener("DOMContentLoaded", init);

async function init() {
    initTheme();
    bindEvents();
    elements.taskDueDate.value = toDateKey(new Date());
    populateTimezones();

    try {
        state.user = await request("/api/auth/me");
        const [tasks, settings] = await Promise.all([
            request("/api/tasks"),
            request("/api/settings")
        ]);
        state.tasks = tasks;
        state.settings = settings;
        renderUser();
        renderSettings();
        renderDashboard();
    } catch (error) {
        window.location.assign("/");
    }
}

function bindEvents() {
    elements.themeToggle.addEventListener("change", () => {
        setTheme(elements.themeToggle.checked ? "dark" : "light");
    });

    elements.logoutButton.addEventListener("click", async () => {
        await fetch("/api/auth/logout", {method: "POST", credentials: "same-origin"});
        window.location.assign("/");
    });

    elements.taskForm.addEventListener("submit", saveTask);
    elements.cancelEditButton.addEventListener("click", resetTaskForm);
    elements.taskList.addEventListener("click", handleTaskListClick);
    elements.taskList.addEventListener("change", handleTaskListChange);

    elements.tabs.forEach((tab) => {
        tab.addEventListener("click", () => {
            state.activeView = tab.dataset.view;
            renderDashboard();
        });
    });

    elements.previousMonth.addEventListener("click", () => {
        state.calendarMonth = new Date(
                state.calendarMonth.getFullYear(),
                state.calendarMonth.getMonth() - 1,
                1
        );
        renderCalendar();
    });

    elements.nextMonth.addEventListener("click", () => {
        state.calendarMonth = new Date(
                state.calendarMonth.getFullYear(),
                state.calendarMonth.getMonth() + 1,
                1
        );
        renderCalendar();
    });

    elements.digestDaily.addEventListener("change", () => keepSingleDigestChoice("DAILY"));
    elements.digestWeekly.addEventListener("change", () => keepSingleDigestChoice("WEEKLY"));
    elements.settingsForm.addEventListener("submit", saveSettings);
}

function renderUser() {
    elements.welcomeLabel.textContent = `Welcome back, ${state.user.displayName}`;
}

function renderDashboard() {
    elements.tabs.forEach((tab) => {
        tab.classList.toggle("active", tab.dataset.view === state.activeView);
    });

    const showingSettings = state.activeView === "settings";
    elements.tasksView.hidden = showingSettings;
    elements.settingsView.hidden = !showingSettings;

    if (!showingSettings) {
        renderTasks();
    }

    renderProgress();
    renderCalendar();
}

function renderTasks() {
    const visibleTasks = getVisibleTasks();
    const isUpcoming = state.activeView === "upcoming";

    elements.taskListTitle.textContent = isUpcoming ? "Upcoming This Week" : "All Tasks";
    elements.taskListKicker.textContent = isUpcoming ? "Next 7 days" : "Current workload";
    elements.visibleCount.textContent = String(visibleTasks.length);
    elements.taskList.innerHTML = "";

    if (visibleTasks.length === 0) {
        const empty = document.createElement("div");
        empty.className = "empty-state";
        empty.textContent = isUpcoming ? "No tasks due in the next 7 days." : "Create your first task to start tracking progress.";
        elements.taskList.append(empty);
        return;
    }

    visibleTasks.forEach((task) => {
        elements.taskList.append(createTaskCard(task));
    });
}

function createTaskCard(task) {
    const article = document.createElement("article");
    article.className = `task-card priority-${task.priority.toLowerCase()} ${task.completed ? "is-complete" : ""}`.trim();

    article.innerHTML = `
        <input class="task-check" type="checkbox" data-action="toggle" data-id="${task.id}" ${task.completed ? "checked" : ""} aria-label="Toggle ${escapeHtml(task.title)}">
        <div class="task-body">
            <h3 class="task-title">${escapeHtml(task.title)}</h3>
            <p class="task-description">${escapeHtml(task.description || "")}</p>
            <div class="task-meta">
                <span class="tag">${formatCategory(task.category)}</span>
                <span class="tag priority-${task.priority.toLowerCase()}">${formatPriority(task.priority)}</span>
                <span class="tag">Due ${formatDate(task.dueDate)}</span>
            </div>
        </div>
        <div class="task-actions">
            <button class="icon-button" type="button" data-action="edit" data-id="${task.id}" aria-label="Edit ${escapeHtml(task.title)}">&#9998;</button>
            <button class="icon-button" type="button" data-action="delete" data-id="${task.id}" aria-label="Delete ${escapeHtml(task.title)}">&times;</button>
        </div>
    `;

    return article;
}

async function saveTask(event) {
    event.preventDefault();

    const payload = getTaskPayload();
    if (!payload.title) {
        showToast("Task title is required.");
        elements.taskTitle.focus();
        return;
    }

    elements.taskSubmitButton.disabled = true;

    try {
        const editing = state.editingTaskId !== null;
        const endpoint = editing ? `/api/tasks/${state.editingTaskId}` : "/api/tasks";
        const method = editing ? "PUT" : "POST";
        await request(endpoint, {
            method,
            body: JSON.stringify(payload)
        });
        state.tasks = await request("/api/tasks");
        resetTaskForm();
        renderDashboard();
        showToast(editing ? "Task updated." : "Task created.");
    } catch (error) {
        showToast(error.message);
    } finally {
        elements.taskSubmitButton.disabled = false;
    }
}

function getTaskPayload() {
    const existingTask = state.tasks.find((task) => task.id === state.editingTaskId);
    return {
        title: elements.taskTitle.value.trim(),
        description: elements.taskDescription.value.trim(),
        category: elements.taskCategory.value,
        priority: elements.taskPriority.value,
        dueDate: elements.taskDueDate.value,
        completed: existingTask ? existingTask.completed : false
    };
}

async function handleTaskListChange(event) {
    if (event.target.dataset.action !== "toggle") {
        return;
    }

    const taskId = Number(event.target.dataset.id);
    const completed = event.target.checked;

    try {
        const updatedTask = await request(`/api/tasks/${taskId}/completion`, {
            method: "PATCH",
            body: JSON.stringify({completed})
        });
        state.tasks = state.tasks.map((task) => task.id === taskId ? updatedTask : task);
        renderDashboard();
    } catch (error) {
        event.target.checked = !completed;
        showToast(error.message);
    }
}

async function handleTaskListClick(event) {
    const actionButton = event.target.closest("[data-action]");
    if (!actionButton || actionButton.dataset.action === "toggle") {
        return;
    }

    const taskId = Number(actionButton.dataset.id);
    const task = state.tasks.find((item) => item.id === taskId);

    if (!task) {
        return;
    }

    if (actionButton.dataset.action === "edit") {
        startEditingTask(task);
        return;
    }

    if (actionButton.dataset.action === "delete") {
        const confirmed = window.confirm(`Delete "${task.title}"?`);
        if (!confirmed) {
            return;
        }

        try {
            await request(`/api/tasks/${task.id}`, {method: "DELETE"});
            state.tasks = state.tasks.filter((item) => item.id !== task.id);
            if (state.editingTaskId === task.id) {
                resetTaskForm();
            }
            renderDashboard();
            showToast("Task deleted.");
        } catch (error) {
            showToast(error.message);
        }
    }
}

function startEditingTask(task) {
    state.editingTaskId = task.id;
    elements.taskTitle.value = task.title;
    elements.taskDescription.value = task.description || "";
    elements.taskCategory.value = task.category;
    elements.taskPriority.value = task.priority;
    elements.taskDueDate.value = task.dueDate;
    elements.taskFormTitle.textContent = "Edit task";
    elements.taskSubmitButton.textContent = "Update task";
    elements.cancelEditButton.hidden = false;
    elements.taskTitle.focus();
}

function resetTaskForm() {
    state.editingTaskId = null;
    elements.taskForm.reset();
    elements.taskDueDate.value = toDateKey(new Date());
    elements.taskFormTitle.textContent = "New task";
    elements.taskSubmitButton.textContent = "Create task";
    elements.cancelEditButton.hidden = true;
}

function renderProgress() {
    const total = state.tasks.length;
    const completed = state.tasks.filter((task) => task.completed).length;
    const percent = total === 0 ? 0 : Math.round((completed / total) * 100);

    elements.progressLabel.textContent = `${percent}% complete`;
    elements.taskCountLabel.textContent = total === 1 ? "1 task" : `${total} tasks`;
    elements.progressFill.style.width = `${percent}%`;
}

function getVisibleTasks() {
    if (state.activeView !== "upcoming") {
        return state.tasks;
    }

    const today = toDateKey(new Date());
    const nextWeek = toDateKey(addDays(new Date(), 7));
    return state.tasks.filter((task) => task.dueDate >= today && task.dueDate <= nextWeek);
}

function renderSettings() {
    if (!state.settings) {
        return;
    }

    elements.digestDaily.checked = state.settings.digestFrequency === "DAILY";
    elements.digestWeekly.checked = state.settings.digestFrequency === "WEEKLY";
    elements.examReminders.checked = state.settings.examReminders;
    elements.assignmentReminders.checked = state.settings.assignmentReminders;

    ensureTimezoneOption(state.settings.timezone);
    elements.timezoneSelect.value = state.settings.timezone;
}

async function saveSettings(event) {
    event.preventDefault();

    const payload = {
        digestFrequency: elements.digestDaily.checked ? "DAILY" : "WEEKLY",
        examReminders: elements.examReminders.checked,
        assignmentReminders: elements.assignmentReminders.checked,
        timezone: elements.timezoneSelect.value
    };

    try {
        state.settings = await request("/api/settings", {
            method: "PUT",
            body: JSON.stringify(payload)
        });
        renderSettings();
        showToast("Settings saved.");
    } catch (error) {
        showToast(error.message);
    }
}

function keepSingleDigestChoice(selected) {
    if (selected === "DAILY") {
        elements.digestDaily.checked = true;
        elements.digestWeekly.checked = false;
        return;
    }

    elements.digestDaily.checked = false;
    elements.digestWeekly.checked = true;
}

function populateTimezones() {
    const localTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC";
    const timezones = [
        "UTC",
        localTimezone,
        "America/New_York",
        "America/Chicago",
        "America/Denver",
        "America/Los_Angeles",
        "Europe/London",
        "Europe/Athens",
        "Europe/Berlin",
        "Asia/Dubai",
        "Asia/Kolkata",
        "Asia/Tokyo",
        "Australia/Sydney"
    ];

    [...new Set(timezones)].sort().forEach((timezone) => {
        const option = document.createElement("option");
        option.value = timezone;
        option.textContent = timezone.replace("_", " ");
        elements.timezoneSelect.append(option);
    });
}

function ensureTimezoneOption(timezone) {
    const exists = [...elements.timezoneSelect.options].some((option) => option.value === timezone);
    if (exists) {
        return;
    }

    const option = document.createElement("option");
    option.value = timezone;
    option.textContent = timezone.replace("_", " ");
    elements.timezoneSelect.append(option);
}

function renderCalendar() {
    const month = state.calendarMonth.getMonth();
    const year = state.calendarMonth.getFullYear();
    const monthName = state.calendarMonth.toLocaleDateString(undefined, {
        month: "long",
        year: "numeric"
    });

    elements.calendarTitle.textContent = monthName;
    elements.calendarGrid.innerHTML = "";

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    for (let i = 0; i < firstDay; i += 1) {
        const blank = document.createElement("div");
        blank.className = "calendar-day blank";
        elements.calendarGrid.append(blank);
    }

    const todayKey = toDateKey(new Date());

    for (let day = 1; day <= daysInMonth; day += 1) {
        const dateKey = `${year}-${pad(month + 1)}-${pad(day)}`;
        const dayCell = document.createElement("div");
        dayCell.className = `calendar-day ${dateKey === todayKey ? "today" : ""}`.trim();
        dayCell.setAttribute("aria-label", calendarLabel(dateKey));

        const dayNumber = document.createElement("span");
        dayNumber.textContent = String(day);
        dayCell.append(dayNumber);

        const dots = document.createElement("div");
        dots.className = "day-dots";
        getTaskPrioritiesForDate(dateKey).slice(0, 3).forEach((priority) => {
            const dot = document.createElement("span");
            dot.className = `day-dot ${priority.toLowerCase()}`;
            dots.append(dot);
        });
        dayCell.append(dots);
        elements.calendarGrid.append(dayCell);
    }
}

function getTaskPrioritiesForDate(dateKey) {
    return state.tasks
            .filter((task) => task.dueDate === dateKey)
            .sort((a, b) => priorityRank(a.priority) - priorityRank(b.priority))
            .map((task) => task.priority);
}

function calendarLabel(dateKey) {
    const count = state.tasks.filter((task) => task.dueDate === dateKey).length;
    const dateLabel = formatDate(dateKey);
    return count === 0 ? dateLabel : `${dateLabel}, ${count} task${count === 1 ? "" : "s"} due`;
}

function priorityRank(priority) {
    return {HIGH: 1, MEDIUM: 2, LOW: 3}[priority] || 4;
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

    if (response.status === 401) {
        throw new Error("Authentication required.");
    }

    if (!response.ok) {
        const payload = await safeJson(response);
        throw new Error(payload.message || "Unable to complete the request.");
    }

    return safeJson(response);
}

async function safeJson(response) {
    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

function initTheme() {
    setTheme(localStorage.getItem(THEME_KEY) || "light");
}

function setTheme(theme) {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem(THEME_KEY, theme);
    elements.themeToggle.checked = theme === "dark";
}

function showToast(message) {
    elements.toast.textContent = message;
    elements.toast.classList.add("show");
    window.clearTimeout(showToast.timeoutId);
    showToast.timeoutId = window.setTimeout(() => {
        elements.toast.classList.remove("show");
    }, 2600);
}

function addDays(date, days) {
    const copy = new Date(date);
    copy.setDate(copy.getDate() + days);
    return copy;
}

function toDateKey(date) {
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

function pad(value) {
    return String(value).padStart(2, "0");
}

function formatDate(dateKey) {
    const [year, month, day] = dateKey.split("-").map(Number);
    return new Date(year, month - 1, day).toLocaleDateString(undefined, {
        month: "short",
        day: "numeric",
        year: "numeric"
    });
}

function formatCategory(category) {
    return {
        PERSONAL: "Personal",
        EXAM: "Exam",
        ASSIGNMENT: "Assignment"
    }[category] || category;
}

function formatPriority(priority) {
    return {
        HIGH: "High priority",
        MEDIUM: "Medium priority",
        LOW: "Low priority"
    }[priority] || priority;
}

function escapeHtml(value) {
    return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
}
