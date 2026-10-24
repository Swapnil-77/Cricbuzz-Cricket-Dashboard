
const API_URL = "http://localhost:8080/api/v1/matches";
const TEAM_API_URL = "http://localhost:8080/api/v1/teams";

const PAGE_SIZE = 6;
const AUTO_REFRESH_MS = 15000;

let allMatches = [];
let allTeams = [];
let filteredMatches = [];
let currentPage = 0;
let toastTimer;

const $ = id => document.getElementById(id);

const elements = {
    apiStatus: $("apiStatus"),
    apiDot: $("apiDot"),
    refreshBtn: $("refreshBtn"),
    openAddBtn: $("openAddBtn"),

    totalMatches: $("totalMatches"),
    liveMatches: $("liveMatches"),
    upcomingMatches: $("upcomingMatches"),
    completedMatches: $("completedMatches"),

    searchForm: $("searchForm"),
    teamSearch: $("teamSearch"),
    statusFilter: $("statusFilter"),
    sortFilter: $("sortFilter"),
    resetBtn: $("resetBtn"),

    matchesContainer: $("matchesContainer"),
    loadingContainer: $("loadingContainer"),
    errorContainer: $("errorContainer"),
    errorText: $("errorText"),
    retryBtn: $("retryBtn"),
    emptyState: $("emptyState"),
    emptyResetBtn: $("emptyResetBtn"),

    pageInfo: $("pageInfo"),
    prevPageBtn: $("prevPageBtn"),
    nextPageBtn: $("nextPageBtn"),
    matchCount: $("matchCount"),

    matchModal: $("matchModal"),
    matchForm: $("matchForm"),
    matchModalTitle: $("matchModalTitle"),
    matchId: $("matchId"),
    team1Id: $("team1Id"),
    team2Id: $("team2Id"),
    matchType: $("matchType"),
    matchDate: $("matchDate"),
    venue: $("venue"),
    saveMatchBtn: $("saveMatchBtn"),
    closeModalBtn: $("closeModalBtn"),
    cancelModalBtn: $("cancelModalBtn"),

    detailsModal: $("detailsModal"),
    detailsTitle: $("detailsTitle"),
    detailsContent: $("detailsContent"),
    closeDetailsBtn: $("closeDetailsBtn"),
    closeDetailsAction: $("closeDetailsAction"),

    toast: $("toast")
};

async function apiRequest(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...options.headers
        }
    });

    if (!response.ok) {
        let message = `Request failed (${response.status})`;

        try {
            const data = await response.json();
            message = data.message || data.detail || data.error || message;
        } catch {
            try {
                const text = await response.text();
                if (text) message = text;
            } catch {
                // Keep the default error message.
            }
        }

        throw new Error(message);
    }

    if (response.status === 204) return null;

    const contentType = response.headers.get("content-type") || "";

    if (contentType.includes("application/json")) {
        return response.json();
    }

    return null;
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

function showToast(message, isError = false) {
    clearTimeout(toastTimer);

    elements.toast.textContent = message;
    elements.toast.classList.toggle("error", isError);
    elements.toast.classList.add("show");

    toastTimer = setTimeout(() => {
        elements.toast.classList.remove("show");
    }, 3500);
}

function setApiStatus(online) {
    elements.apiStatus.textContent = online ? "API Connected" : "API Offline";
    elements.apiDot.classList.toggle("online", online);
    elements.apiDot.classList.toggle("offline", !online);
}

function setLoading(show) {
    elements.loadingContainer.hidden = !show;
}

function setError(message) {
    elements.errorText.textContent = message;
    elements.errorContainer.hidden = false;
}

function hideError() {
    elements.errorContainer.hidden = true;
}

function getScore(score) {
    return score || { runs: 0, wickets: 0, overs: 0 };
}

function formatScore(score) {
    const value = getScore(score);
    return `${Number(value.runs) || 0}/${Number(value.wickets) || 0}`;
}

function formatOvers(score) {
    const value = getScore(score);
    return `${Number(value.overs) || 0} overs`;
}

function formatDate(dateValue) {
    if (!dateValue) return "Date not available";

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
        return String(dateValue);
    }

    return date.toLocaleString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function formatStatus(status) {
    return String(status || "UNKNOWN").replaceAll("_", " ");
}

function getStatusClass(status) {
    switch (status) {
        case "LIVE":
            return "status-live";
        case "COMPLETED":
            return "status-completed";
        default:
            return "status-upcoming";
    }
}

function getTeamName(match, number) {
    return match[`team${number}Name`] ||
        `Team ${match[`team${number}Id`] ?? number}`;
}
 
function getResult(match) {
    if (match.status !== "COMPLETED") {
        if (match.status === "LIVE") {
            return "Match in progress";
        }

        return "Match has not started";
    }

    const score1 = getScore(match.team1Score);
    const score2 = getScore(match.team2Score);

    if (score1.runs === score2.runs) {
        return "Match tied";
    }

    const team1Won = score1.runs > score2.runs;

    const winner = team1Won
        ? getTeamName(match, 1)
        : getTeamName(match, 2);

    const winningScore = team1Won ? score1 : score2;
    const losingScore = team1Won ? score2 : score1;

    const winningRuns = Math.abs(score1.runs - score2.runs);

    // Agar losing team all-out ho gayi
    if (losingScore.wickets >= 10) {
        return `${winner} won by ${winningRuns} runs`;
    }

    // Winning team ne chase karke match jeeta
    const wicketsRemaining = 10 - winningScore.wickets;

    return `${winner} won by ${wicketsRemaining} wickets`;
}

/* LOAD TEAMS */

async function loadTeams() {
    const data = await apiRequest(`${TEAM_API_URL}?page=0&size=100`);

    const teams = Array.isArray(data) ? data : data.content;

    if (!Array.isArray(teams)) {
        throw new Error("Unexpected response from teams API");
    }

    allTeams = teams;

    populateTeamDropdown(elements.team1Id);
    populateTeamDropdown(elements.team2Id);
}

function populateTeamDropdown(selectElement) {
    selectElement.innerHTML = '<option value="">Select Team</option>';

    allTeams.forEach(team => {
        const option = document.createElement("option");
        option.value = team.id;
        option.textContent = team.name;
        selectElement.appendChild(option);
    });
}

/* LOAD MATCHES */

async function loadMatches(showLoading = true) {
    if (showLoading) {
        setLoading(true);
        hideError();
    }

    try {
        const data = await apiRequest(
            `${API_URL}?page=0&size=100&sort=matchDate,desc`
        );

        const matches = Array.isArray(data) ? data : data.content;

        if (!Array.isArray(matches)) {
            throw new Error("Unexpected response from matches API");
        }

        allMatches = matches;

        setApiStatus(true);
        updateStatistics();
        applyFilters();
    } catch (error) {
        setApiStatus(false);

        if (showLoading || allMatches.length === 0) {
            setError(
                `${error.message}. Ensure the Spring Boot backend is running on port 8080.`
            );
        } else {
            showToast("Could not refresh matches: " + error.message, true);
        }
    } finally {
        setLoading(false);
    }
}

function updateStatistics() {
    elements.totalMatches.textContent = allMatches.length;
    elements.liveMatches.textContent =
        allMatches.filter(m => m.status === "LIVE").length;
    elements.upcomingMatches.textContent =
        allMatches.filter(m => m.status === "UPCOMING").length;
    elements.completedMatches.textContent =
        allMatches.filter(m => m.status === "COMPLETED").length;
}

/* SEARCH, FILTER AND SORT */

function applyFilters() {
    const query = elements.teamSearch.value.trim().toLowerCase();
    const status = elements.statusFilter.value;
    const sort = elements.sortFilter.value;

    filteredMatches = allMatches.filter(match => {
        const searchable = [
            getTeamName(match, 1),
            getTeamName(match, 2),
            match.venue,
            match.matchType
        ].join(" ").toLowerCase();

        const matchesSearch = !query || searchable.includes(query);
        const matchesStatus = !status || match.status === status;

        return matchesSearch && matchesStatus;
    });

    if (sort === "oldest") {
        filteredMatches.sort(
            (a, b) => new Date(a.matchDate) - new Date(b.matchDate)
        );
    } else if (sort === "team") {
        filteredMatches.sort((a, b) =>
            getTeamName(a, 1).localeCompare(getTeamName(b, 1))
        );
    } else {
        filteredMatches.sort(
            (a, b) => new Date(b.matchDate) - new Date(a.matchDate)
        );
    }

    currentPage = 0;
    renderMatches();
}

/* RENDER MATCH CARDS */

function renderMatches() {
    elements.matchesContainer.innerHTML = "";

    const total = filteredMatches.length;
    const pageCount = Math.max(1, Math.ceil(total / PAGE_SIZE));

    currentPage = Math.min(currentPage, pageCount - 1);

    elements.matchCount.textContent =
        `${total} ${total === 1 ? "match" : "matches"}`;

    elements.pageInfo.textContent =
        `Page ${currentPage + 1} of ${pageCount}`;

    elements.prevPageBtn.disabled = currentPage === 0;
    elements.nextPageBtn.disabled = currentPage >= pageCount - 1;

    elements.emptyState.hidden = total !== 0;

    if (total === 0) return;

    const start = currentPage * PAGE_SIZE;
    const pageMatches = filteredMatches.slice(start, start + PAGE_SIZE);

    elements.matchesContainer.innerHTML = pageMatches
        .map(renderMatchCard)
        .join("");
}

function renderMatchCard(match) {
    const status = match.status || "UPCOMING";
    const score1 = getScore(match.team1Score);
    const score2 = getScore(match.team2Score);

    const statusAction = status === "UPCOMING"
        ? `<button class="btn-small" data-action="status" data-id="${match.id}">
               ▶ Start Match
           </button>`
        : status === "LIVE"
            ? `<button class="btn-small" data-action="status" data-id="${match.id}">
                   ✓ Complete
               </button>`
            : "";

    const scoreAction = status === "LIVE"
        ? `<button class="btn-small" data-action="score" data-id="${match.id}">
               Update Score
           </button>`
        : "";

    return `
        <article class="match-card">
            <div class="match-card-header">
                <div>
                    <span class="match-type">
                        ${escapeHtml(match.matchType || "CRICKET")}
                    </span>
                    <p class="match-venue">
                        📍 ${escapeHtml(match.venue || "Venue unavailable")}
                    </p>
                </div>

                <span class="status-badge ${getStatusClass(status)}">
                    ${escapeHtml(formatStatus(status))}
                </span>
            </div>

            <div class="match-teams">
                <div class="team-block">
                    <p class="team-name">${escapeHtml(getTeamName(match, 1))}</p>
                    <p class="team-score">${escapeHtml(formatScore(score1))}</p>
                    <p class="team-overs">${escapeHtml(formatOvers(score1))}</p>
                </div>

                <span class="vs-label">VS</span>

                <div class="team-block">
                    <p class="team-name">${escapeHtml(getTeamName(match, 2))}</p>
                    <p class="team-score">${escapeHtml(formatScore(score2))}</p>
                    <p class="team-overs">${escapeHtml(formatOvers(score2))}</p>
                </div>
            </div>

            <div class="match-result">
                ${escapeHtml(getResult(match))}
            </div>

            <p class="match-date">🕒 ${escapeHtml(formatDate(match.matchDate))}</p>

            <div class="card-actions">
                <button class="btn-small" data-action="details" data-id="${match.id}">
                    View Details
                </button>

                ${statusAction}
                ${scoreAction}

                <button class="btn-small" data-action="edit" data-id="${match.id}">
                    Edit
                </button>

                <button class="btn-small danger" data-action="delete" data-id="${match.id}">
                    Delete
                </button>
            </div>
        </article>
    `;
}

/* ADD / EDIT MATCH */

function openMatchModal(match = null) {
    elements.matchForm.reset();
    elements.matchId.value = match ? match.id : "";
    elements.matchModalTitle.textContent = match ? "Edit Match" : "Add New Match";
    elements.saveMatchBtn.textContent = match ? "Update Match" : "Save Match";

    if (match) {
        elements.team1Id.value = match.team1Id ?? "";
        elements.team2Id.value = match.team2Id ?? "";
        elements.matchType.value = match.matchType ?? "";
        elements.venue.value = match.venue ?? "";

        if (match.matchDate) {
            const date = new Date(match.matchDate);
            const localDate = new Date(
                date.getTime() - date.getTimezoneOffset() * 60000
            );

            elements.matchDate.value = localDate.toISOString().slice(0, 16);
        }
    }

    elements.matchModal.hidden = false;
}

function closeMatchModal() {
    elements.matchModal.hidden = true;
}

async function saveMatch(event) {
    event.preventDefault();

    const team1Id = Number(elements.team1Id.value);
    const team2Id = Number(elements.team2Id.value);

    if (!team1Id || !team2Id) {
        showToast("Please select both teams.", true);
        return;
    }

    if (team1Id === team2Id) {
        showToast("A team cannot play against itself.", true);
        return;
    }

    const matchId = elements.matchId.value;

    const payload = {
        team1Id,
        team2Id,
        venue: elements.venue.value.trim(),
        matchDate: elements.matchDate.value,
        matchType: elements.matchType.value
    };

    const isEdit = Boolean(matchId);

    elements.saveMatchBtn.disabled = true;
    elements.saveMatchBtn.textContent = "Saving...";

    try {
        await apiRequest(
            isEdit ? `${API_URL}/${matchId}` : API_URL,
            {
                method: isEdit ? "PUT" : "POST",
                body: JSON.stringify(payload)
            }
        );

        closeMatchModal();
        showToast(isEdit ? "Match updated successfully." : "Match created successfully.");

        await loadMatches();
    } catch (error) {
        showToast(error.message, true);
    } finally {
        elements.saveMatchBtn.disabled = false;
        elements.saveMatchBtn.textContent = isEdit ? "Update Match" : "Save Match";
    }
}

function editMatch(id) {
    const match = allMatches.find(m => String(m.id) === String(id));

    if (!match) {
        showToast("Match not found.", true);
        return;
    }

    openMatchModal(match);
}

/* VIEW DETAILS */

function viewDetails(id) {
    const match = allMatches.find(m => String(m.id) === String(id));

    if (!match) {
        showToast("Match not found.", true);
        return;
    }

    const score1 = getScore(match.team1Score);
    const score2 = getScore(match.team2Score);

    elements.detailsTitle.textContent =
        `${getTeamName(match, 1)} vs ${getTeamName(match, 2)}`;

    elements.detailsContent.innerHTML = `
        <div class="detail-score">
            <div class="detail-team">
                <h3>${escapeHtml(getTeamName(match, 1))}</h3>
                <strong>${escapeHtml(formatScore(score1))}</strong>
                <p>${escapeHtml(formatOvers(score1))}</p>
            </div>

            <div class="detail-team">
                <h3>${escapeHtml(getTeamName(match, 2))}</h3>
                <strong>${escapeHtml(formatScore(score2))}</strong>
                <p>${escapeHtml(formatOvers(score2))}</p>
            </div>
        </div>

        <div class="match-result">
            ${escapeHtml(getResult(match))}
        </div>

        <div class="detail-meta" style="margin-top: 16px;">
            <div>
                <span>Match Format</span>
                <strong>${escapeHtml(match.matchType || "—")}</strong>
            </div>

            <div>
                <span>Status</span>
                <strong>${escapeHtml(formatStatus(match.status))}</strong>
            </div>

            <div>
                <span>Venue</span>
                <strong>${escapeHtml(match.venue || "—")}</strong>
            </div>

            <div>
                <span>Date & Time</span>
                <strong>${escapeHtml(formatDate(match.matchDate))}</strong>
            </div>

            <div>
                <span>Match ID</span>
                <strong>#${escapeHtml(match.id)}</strong>
            </div>
        </div>
    `;

    elements.detailsModal.hidden = false;
}

function closeDetailsModal() {
    elements.detailsModal.hidden = true;
}

/* UPDATE SCORE */

async function updateScore(id) {
    const match = allMatches.find(m => String(m.id) === String(id));

    if (!match) return;

    const score1 = getScore(match.team1Score);
    const score2 = getScore(match.team2Score);

    const team1Runs = prompt(
        `${getTeamName(match, 1)} - Enter runs:`,
        score1.runs
    );

    if (team1Runs === null) return;

    const team1Wickets = prompt(
        `${getTeamName(match, 1)} - Enter wickets (0-10):`,
        score1.wickets
    );

    if (team1Wickets === null) return;

    const team1Overs = prompt(
        `${getTeamName(match, 1)} - Enter overs:`,
        score1.overs
    );

    if (team1Overs === null) return;

    const team2Runs = prompt(
        `${getTeamName(match, 2)} - Enter runs:`,
        score2.runs
    );

    if (team2Runs === null) return;

    const team2Wickets = prompt(
        `${getTeamName(match, 2)} - Enter wickets (0-10):`,
        score2.wickets
    );

    if (team2Wickets === null) return;

    const team2Overs = prompt(
        `${getTeamName(match, 2)} - Enter overs:`,
        score2.overs
    );

    if (team2Overs === null) return;

    const values = [
        team1Runs, team1Wickets, team1Overs,
        team2Runs, team2Wickets, team2Overs
    ].map(Number);

    if (values.some(value => !Number.isFinite(value) || value < 0)) {
        showToast("Enter valid non-negative numbers.", true);
        return;
    }

    if (
        !Number.isInteger(values[0]) ||
        !Number.isInteger(values[1]) ||
        !Number.isInteger(values[3]) ||
        !Number.isInteger(values[4]) ||
        values[1] > 10 ||
        values[4] > 10
    ) {
        showToast("Runs and wickets must be whole numbers; wickets must be 0-10.", true);
        return;
    }

    const payload = {
        team1: {
            runs: values[0],
            wickets: values[1],
            overs: values[2]
        },
        team2: {
            runs: values[3],
            wickets: values[4],
            overs: values[5]
        }
    };

    try {
        await apiRequest(`${API_URL}/${id}/score`, {
            method: "PATCH",
            body: JSON.stringify(payload)
        });

        showToast("Score updated successfully.");
        await loadMatches();
    } catch (error) {
        showToast(error.message, true);
    }
}

/* UPDATE MATCH STATUS */

async function updateStatus(id) {
    const match = allMatches.find(m => String(m.id) === String(id));

    if (!match) return;

    const nextStatus = match.status === "UPCOMING" ? "LIVE" :
        match.status === "LIVE" ? "COMPLETED" : null;

    if (!nextStatus) return;

    const confirmed = confirm(
        `Change match status from ${match.status} to ${nextStatus}?`
    );

    if (!confirmed) return;

    try {
        await apiRequest(`${API_URL}/${id}/status`, {
            method: "PATCH",
            body: JSON.stringify({ status: nextStatus })
        });

        showToast(`Match status changed to ${nextStatus}.`);
        await loadMatches();
    } catch (error) {
        showToast(error.message, true);
    }
}

/* DELETE MATCH */

async function deleteMatch(id) {
    const match = allMatches.find(m => String(m.id) === String(id));

    if (!match) return;

    const confirmed = confirm(
        `Delete ${getTeamName(match, 1)} vs ${getTeamName(match, 2)}?`
    );

    if (!confirmed) return;

    try {
        await apiRequest(`${API_URL}/${id}`, {
            method: "DELETE"
        });

        showToast("Match deleted successfully.");
        await loadMatches();
    } catch (error) {
        showToast(error.message, true);
    }
}

/* EVENTS */

elements.searchForm.addEventListener("submit", event => {
    event.preventDefault();
    applyFilters();
});

elements.teamSearch.addEventListener("input", applyFilters);
elements.statusFilter.addEventListener("change", applyFilters);
elements.sortFilter.addEventListener("change", applyFilters);

elements.resetBtn.addEventListener("click", resetFilters);
elements.emptyResetBtn.addEventListener("click", resetFilters);

function resetFilters() {
    elements.teamSearch.value = "";
    elements.statusFilter.value = "";
    elements.sortFilter.value = "newest";
    applyFilters();
}

elements.refreshBtn.addEventListener("click", () => loadMatches());
elements.retryBtn.addEventListener("click", initialise);

elements.openAddBtn.addEventListener("click", () => openMatchModal());
elements.closeModalBtn.addEventListener("click", closeMatchModal);
elements.cancelModalBtn.addEventListener("click", closeMatchModal);
elements.matchForm.addEventListener("submit", saveMatch);

elements.closeDetailsBtn.addEventListener("click", closeDetailsModal);
elements.closeDetailsAction.addEventListener("click", closeDetailsModal);

elements.prevPageBtn.addEventListener("click", () => {
    if (currentPage > 0) {
        currentPage--;
        renderMatches();
        window.scrollTo({ top: 0, behavior: "smooth" });
    }
});

elements.nextPageBtn.addEventListener("click", () => {
    const pageCount = Math.ceil(filteredMatches.length / PAGE_SIZE);

    if (currentPage < pageCount - 1) {
        currentPage++;
        renderMatches();
        window.scrollTo({ top: 0, behavior: "smooth" });
    }
});

elements.matchesContainer.addEventListener("click", event => {
    const button = event.target.closest("button[data-action]");

    if (!button) return;

    const { action, id } = button.dataset;

    switch (action) {
        case "details":
            viewDetails(id);
            break;
        case "edit":
            editMatch(id);
            break;
        case "score":
            updateScore(id);
            break;
        case "status":
            updateStatus(id);
            break;
        case "delete":
            deleteMatch(id);
            break;
    }
});

elements.matchModal.addEventListener("click", event => {
    if (event.target === elements.matchModal) closeMatchModal();
});

elements.detailsModal.addEventListener("click", event => {
    if (event.target === elements.detailsModal) closeDetailsModal();
});

document.addEventListener("keydown", event => {
    if (event.key === "Escape") {
        closeMatchModal();
        closeDetailsModal();
    }
});

/* START APPLICATION */

async function initialise() {
    setLoading(true);
    hideError();

    try {
        await loadTeams();
        await loadMatches();
    } catch (error) {
        setApiStatus(false);
        setLoading(false);
        setError(
            `${error.message}. Check that your backend and team API are running.`
        );
    }
}

initialise();

setInterval(() => {
    if (!document.hidden && elements.matchModal.hidden && elements.detailsModal.hidden) {
        loadMatches(false);
    }
}, AUTO_REFRESH_MS);
