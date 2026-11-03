(() => {
    const form = document.getElementById('questionnaire');
    if (!form) return;
    const panel = document.getElementById('progress-panel');
    const progress = document.getElementById('answer-progress');
    const label = document.getElementById('progress-label');
    const total = form.querySelectorAll('fieldset').length;
    progress.max = total;
    const update = () => {
        const count = form.querySelectorAll('input[type="radio"]:checked').length;
        progress.value = count;
        label.textContent = `${count} von ${total} beantwortet`;
    };
    panel.hidden = false;
    form.addEventListener('change', update);
    window.addEventListener('pageshow', update);
    update();
})();