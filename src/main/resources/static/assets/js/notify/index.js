'use strict';
var notify = $.notify('<i class="fa fa-bell-o"></i><strong>Парақша оқылуда</strong> парақшаны жаппаңыз', {
    type: 'theme',
    allow_dismiss: true,
    delay: 3000,
    showProgressbar: true,
    timer: 300
});

setTimeout(function() {
    notify.update('message', '<i class="fa fa-bell-o"></i><strong>Парақша оқылуда</strong> Деректер қорымен байланыс...');
}, 2000);
