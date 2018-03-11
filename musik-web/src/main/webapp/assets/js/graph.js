var limit = 60, duration = 90, now = new Date(Date.now() - duration);

var width = 500, height = 256;

var groups = {
    output: {
        value: 0,
        color: 'grey',
        data: d3.range(limit).map(function () {
            return 0
        })
    }
};

var x = d3.time.scale()
    .domain([now - (limit - 2), now - duration])
    .range([0, width]);

var y = d3.scale.linear()
    .domain([0, 256])
    .range([height, 0]);

var x_func = function (d, i) {
    return x(now - (limit - 1 - i) * duration)
};

var y_func = function (d) {
    return y(d)
};

var line = d3.svg.line().interpolate('basis').x(x_func).y(y_func);

var svg = d3.select('#graph').append('svg')
    .attr('class', 'chart')
    .attr('width', width)
    .attr('height', height);

var paths = svg.append('g');

for (var name in groups) {
    var group = groups[name];

    group.path = paths.append('path')
        .data([group.data])
        .attr('class', name + ' group')
        .style('stroke', group.color)
}

function tick(value) {
    now = new Date();

    for (var name in groups) {
        var group = groups[name];

        group.data.push(value);
        group.path.attr('d', line)
    }

    // Shift domain
    x.domain([now - (limit - 2) * duration, now - duration]);

    // Slide paths left
    paths.attr('transform', null)
        .transition()
        .duration(duration)
        .ease('linear')
        .attr('transform', 'translate(' + x(now - (limit - 1) * duration) + ')');

    // Remove oldest data point from each group
    for (var idx in groups) {
        groups[idx].data.shift()
    }
}

tick(0);