import 'https://cdn.jsdelivr.net/npm/chart.js';

window.renderPriceChart = (canvas, labels, data, avgData) => {
    const ctx = canvas.getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Product Price',
                    data: data,
                    backgroundColor: 'rgba(54, 162, 235, 0.2)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 2,
                    fill: false
                },
                {
                    label: 'Average',
                    data: avgData,
                    backgroundColor: 'rgb(241,175,85)',
                    borderColor: 'rgb(58,49,49)',
                    borderWidth: 2,
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: false
                }
            }
        }
    });
};