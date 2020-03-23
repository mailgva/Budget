function hide_detail(elm) {
    var tr = $(elm).closest('tr').next('tr')[0];
    tr.style.display = (tr.style.display == 'none') ? '' : 'none';
}

function hide_alldetail(elm) {
    $('tr.budget_detail').css('display', ($(elm).is(':checked') ? 'none' : ''));
}