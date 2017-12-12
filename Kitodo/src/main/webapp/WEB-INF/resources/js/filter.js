function setFilter(filterString) {
    document.getElementById('filterMenu:filterfield').value = filterString;
    applyFilter(filterString);
}

function applyFilter(filterString) {
    var invisibleFilter = document.getElementById('j_id_2q:j_id_2s:j_id_2t:j_id_2u:filter');
    invisibleFilter.value = filterString;
    invisibleFilter.dispatchEvent(new Event('keyup'));
}

window.onload = function () {
    document.getElementById('filterMenu:filterfield').addEventListener('change', function () {
        applyFilter(this.value);
    });
}
