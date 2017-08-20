var limit = 60, duration = 90, now = new Date(Date.now() - duration);

var width = 500, height = 200;

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
    .domain([0, 100])
    .range([height, 0]);

var line = d3.svg.line()
    .interpolate('basis')
    .x(function (d, i) {
        return x(now - (limit - 1 - i) * duration)
    })
    .y(function (d) {
        return y(d)
    });

var svg = d3.select('#graph').append('svg')
    .attr('class', 'chart')
    .attr('width', width)
    .attr('height', height + 50);

/*
var axis = svg.append('g')
    .attr('class', 'x axis')
    .attr('transform', 'translate(0,' + height + ')')
    .call(x.axis = d3.svg.axis().scale(x).orient('bottom'));
*/

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

    // Add new values
    for (var name in groups) {
        var group = groups[name];
        //group.data.push(group.value) // Real values arrive at irregular intervals
        group.data.push(value);
        group.path.attr('d', line)
    }

    // Shift domain
    x.domain([now - (limit - 2) * duration, now - duration]);

    // Slide x-axis left
    /*
    axis.transition()
        .duration(duration)
        .ease('linear')
        .call(x.axis);
    */

    // Slide paths left
    paths.attr('transform', null)
        .transition()
        .duration(duration)
        .ease('linear')
        .attr('transform', 'translate(' + x(now - (limit - 1) * duration) + ')');

    // Remove oldest data point from each group
    for (var name in groups) {
        var group = groups[name];
        group.data.shift()
    }
}

tick(0);