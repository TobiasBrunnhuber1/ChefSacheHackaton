(() => {
    // Fortschrittsanzeige, Rollen-Umschaltung und Misch-Link im Fragebogen initialisieren
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

        // Übernimmt eingegebenen Team-Code und Runde beim Rollenwechsel oder Mischen
        const teamInput = document.getElementById('teamCode');
        const roundInput = document.getElementById('roundName');
        const currentRoleInput = form.querySelector('input[name="role"]');
        const isShuffledNow = new URLSearchParams(window.location.search).get('shuffle') === 'true';

        const buildQuizUrl = (role, shuffle) => {
            const team = teamInput ? teamInput.value.trim() : '';
            const round = roundInput ? roundInput.value.trim() : '';
            const params = new URLSearchParams();
            params.set('role', role);
            if (team) params.set('team', team);
            if (round) params.set('round', round);
            if (shuffle) params.set('shuffle', 'true');
            return `/quiz?${params.toString()}`;
        };

        document.querySelectorAll('[data-role-switch]').forEach((link) => {
            link.addEventListener('click', (event) => {
                event.preventDefault();
                window.location.href = buildQuizUrl(link.getAttribute('data-role-switch'), isShuffledNow);
            });
        });

        document.querySelectorAll('[data-shuffle-switch]').forEach((link) => {
            link.addEventListener('click', (event) => {
                event.preventDefault();
                const targetShuffle = link.getAttribute('data-shuffle-target') === 'true';
                const role = currentRoleInput ? currentRoleInput.value : 'EMPLOYEE';
                window.location.href = buildQuizUrl(role, targetShuffle);
            });
        });
    }

    // Einladungslinks und QR-Code auf der Startseite live aktualisieren
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

        // Baut die Links, den QR-Code und versteckte Formularfelder neu auf
        const syncLinks = () => {
            const team = normalizeTeam(hubTeam.value);
            const round = (hubRound.value || '').trim() || 'Runde 1';
            const leaderPath = `/quiz?role=LEADER&team=${encodeURIComponent(team)}&round=${encodeURIComponent(round)}`;
            const employeePath = `/quiz?role=EMPLOYEE&team=${encodeURIComponent(team)}&round=${encodeURIComponent(round)}`;
            const comparePath = `/compare?team=${encodeURIComponent(team)}&round=${encodeURIComponent(round)}`;
            const fullEmployeeUrl = `${baseUrl}${employeePath}`;

            const leaderDisplay = document.getElementById('leader-share-url');
            const employeeDisplay = document.getElementById('employee-share-url');
            const compareDisplay = document.getElementById('compare-share-url');
            const leaderBtn = document.getElementById('leader-start-btn');
            const employeeBtn = document.getElementById('employee-start-btn');
            const compareBtn = document.getElementById('compare-start-btn');
            const qrImg = document.getElementById('employee-qr-img');
            const settingsTeam = document.getElementById('settings-team');
            const settingsRound = document.getElementById('settings-round');

            if (leaderDisplay) leaderDisplay.textContent = `${baseUrl}${leaderPath}`;
            if (employeeDisplay) employeeDisplay.textContent = fullEmployeeUrl;
            if (compareDisplay) compareDisplay.textContent = `${baseUrl}${comparePath}`;
            if (leaderBtn) leaderBtn.href = leaderPath;
            if (employeeBtn) employeeBtn.href = employeePath;
            if (compareBtn) compareBtn.href = comparePath;
            if (qrImg) qrImg.src = `/qr?text=${encodeURIComponent(fullEmployeeUrl)}`;
            if (settingsTeam) settingsTeam.value = team;
            if (settingsRound) settingsRound.value = round;
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
