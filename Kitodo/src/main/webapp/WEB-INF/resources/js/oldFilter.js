function setFilter(filterString) {
    document.getElementById('filterfield').value = filterString;
    applyFilter(filterString);
}

function applyFilter(filterString) {
    var invisibleFilter = document.getElementById('processform:auflistung:ajaxcolumn:filter');
    invisibleFilter.value = filterString;
    invisibleFilter.dispatchEvent(new Event('keyup'));
}

window.onload = function () {
    document.getElementById('filterfield').addEventListener('change', function () {
        applyFilter(this.value);
    });
    document.getElementById('select').addEventListener('change', function () {
        setFilter(this.value);
        applyFilter(this.value);
    });
}
