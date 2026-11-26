(() => {
    // Fortschrittsanzeige und Rollen-Umschaltung im Fragebogen initialisieren
    const form = document.getElementById('questionnaire');
    if (form) {
        const panel = document.getElementById('progress-panel');
        const progress = document.getElementById('answer-progress');
        const label = document.getElementById('progress-label');
        const total = form.querySelectorAll('fieldset').length;
        progress.max = total;

        // Zählt alle markierten Radio-Buttons und aktualisiert Balken samt Status-Text
        const update = () => {
            const count = form.querySelectorAll('input[type="radio"]:checked').length;
            progress.value = count;
            label.textContent = `${count} von ${total} beantwortet`;
        };
        panel.hidden = false;
        form.addEventListener('change', update);
        window.addEventListener('pageshow', update);
        update();

        // Übernimmt eingegebenen Team-Code und Runde beim Wechsel zwischen Führungskraft und Mitarbeitenden
        const teamInput = document.getElementById('teamCode');
        const roundInput = document.getElementById('roundName');
        document.querySelectorAll('[data-role-switch]').forEach((link) => {
            link.addEventListener('click', (event) => {
                const targetRole = link.getAttribute('data-role-switch');
                const team = teamInput ? teamInput.value.trim() : '';
                const round = roundInput ? roundInput.value.trim() : '';
                const params = new URLSearchParams();
                params.set('role', targetRole);
                if (team) params.set('team', team);
                if (round) params.set('round', round);
                event.preventDefault();
                window.location.href = `/quiz?${params.toString()}`;
            });
        });
    }

    // Einladungslinks auf der Startseite live aktualisieren, sobald Team-Code oder Runde geändert werden
    const hubTeam = document.getElementById('hub-team');
    const hubRound = document.getElementById('hub-round');
    if (hubTeam && hubRound) {
        const baseUrl = hubTeam.getAttribute('data-base-url') || window.location.origin;

        // Normiert den Team-Code analog zur Backend-Logik
        const normalizeTeam = (raw) => {
            const cleaned = (raw || '').trim().toLowerCase()
                .replace(/[^a-z0-9äöüß_-]+/g, '-')
                .replace(/^-+|-+$/g, '');
            return cleaned || 'mein-team';
        };

        // Baut die Links für Führungskraft, Mitarbeitende und Team-Vergleich neu auf
        const syncLinks = () => {
            const team = normalizeTeam(hubTeam.value);
            const round = (hubRound.value || '').trim() || 'Runde 1';
            const leaderPath = `/quiz?role=LEADER&team=${encodeURIComponent(team)}&round=${encodeURIComponent(round)}`;
            const employeePath = `/quiz?role=EMPLOYEE&team=${encodeURIComponent(team)}&round=${encodeURIComponent(round)}`;
            const comparePath = `/compare?team=${encodeURIComponent(team)}&round=${encodeURIComponent(round)}`;

            const leaderDisplay = document.getElementById('leader-share-url');
            const employeeDisplay = document.getElementById('employee-share-url');
            const compareDisplay = document.getElementById('compare-share-url');
            const leaderBtn = document.getElementById('leader-start-btn');
            const employeeBtn = document.getElementById('employee-start-btn');
            const compareBtn = document.getElementById('compare-start-btn');

            if (leaderDisplay) leaderDisplay.textContent = `${baseUrl}${leaderPath}`;
            if (employeeDisplay) employeeDisplay.textContent = `${baseUrl}${employeePath}`;
            if (compareDisplay) compareDisplay.textContent = `${baseUrl}${comparePath}`;
            if (leaderBtn) leaderBtn.href = leaderPath;
            if (employeeBtn) employeeBtn.href = employeePath;
            if (compareBtn) compareBtn.href = comparePath;
        };
        hubTeam.addEventListener('input', syncLinks);
        hubRound.addEventListener('input', syncLinks);
    }

    // Kopiert die angezeigten Einladungslinks in die Zwischenablage
    document.querySelectorAll('.copy-btn').forEach((btn) => {
        btn.addEventListener('click', async () => {
            const targetId = btn.getAttribute('data-copy-target');
            const target = document.getElementById(targetId);
            if (!target) return;
            const text = target.textContent.trim();
            try {
                await navigator.clipboard.writeText(text);
                const original = btn.textContent;
                btn.textContent = 'Kopiert ✓';
                setTimeout(() => {
                    btn.textContent = original;
                }, 1800);
            } catch {
                window.prompt('Link kopieren:', text);
            }
        });
    });
})();
