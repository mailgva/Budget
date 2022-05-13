function hideDetail(elm) {
    var tr = $(elm).closest('tr').next('tr')[0];
    tr.style.display = (tr.style.display == 'none') ? '' : 'none';
}

$(function() {
    $('#check-hide').change(function() {
        checked = $(this).prop('checked');
        $('tr.budget_detail').css('display', (checked ? '' : 'none'));
    })
})