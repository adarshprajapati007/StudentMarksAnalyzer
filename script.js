let marks = [];

const el = (id) => document.getElementById(id);

function showMessage(text, isError = false) {
    const msg = el("message");
    msg.textContent = text;
    msg.className = "message " + (isError ? "error" : "success");
}

function renderTable(highlight = []) {
    const body = el("marksBody");

    if (marks.length === 0) {
        body.innerHTML = '<tr><td colspan="2" class="empty">No marks added yet</td></tr>';
        return;
    }

    body.innerHTML = "";
    marks.forEach((mark, i) => {
        const tr = document.createElement("tr");
        if (highlight.includes(i)) {
            tr.className = "highlight";
        }
        tr.innerHTML = `<td>Student ${i + 1}</td><td>${mark}</td>`;
        body.appendChild(tr);
    });
}

function updateStats(stats) {
    el("highest").textContent = stats.highest;
    el("lowest").textContent = stats.lowest;
    el("average").textContent = stats.average;
}

function resetStats() {
    el("highest").textContent = "--";
    el("lowest").textContent = "--";
    el("average").textContent = "--";
}

// Add a mark
el("addBtn").addEventListener("click", () => {
    const input = el("markInput");
    const value = input.value.trim();
    const mark = Number(value);

    if (value === "" || !Number.isInteger(mark) || mark < 0 || mark > 100) {
        showMessage("Marks must be a whole number between 0 and 100.", true);
        return;
    }

    marks.push(mark);
    input.value = "";
    input.focus();
    renderTable();
    showMessage(`Student ${marks.length} added with ${mark} marks.`);
});

// Allow pressing Enter to add a mark
el("markInput").addEventListener("keydown", (e) => {
    if (e.key === "Enter") el("addBtn").click();
});

// Clear all marks
el("clearBtn").addEventListener("click", () => {
    marks = [];
    renderTable();
    resetStats();
    showMessage("All marks cleared.");
});

// Analyze buttons
document.querySelectorAll("[data-action]").forEach((btn) => {
    btn.addEventListener("click", () => analyze(btn.dataset.action));
});

async function analyze(action) {
    if (window.location.protocol === "file:") {
        showMessage("Open http://localhost:8080 in the browser, not the HTML file.", true);
        return;
    }

    if (marks.length === 0) {
        showMessage("Add at least one mark first.", true);
        return;
    }

    const payload = { action, marks };

    if (action === "search") {
        const value = el("searchInput").value.trim();
        const target = Number(value);
        if (value === "" || !Number.isInteger(target)) {
            showMessage("Enter a whole number to search for.", true);
            return;
        }
        payload.target = target;
    }

    try {
        const response = await fetch("/api/analyze", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (!response.ok || !data.ok) {
            showMessage(data.message || "Something went wrong on the server.", true);
            return;
        }

        marks = data.marks;
        renderTable(data.highlight);
        updateStats(data.stats);
        showMessage(data.message);
    } catch (err) {
        showMessage("Could not reach the server. Make sure it is running at http://localhost:8080.", true);
    }
}