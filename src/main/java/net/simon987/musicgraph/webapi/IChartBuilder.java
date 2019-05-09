package net.simon987.musicgraph.webapi;

public interface IChartBuilder {

    byte[] makeChart(ChartOptions options) throws Exception;
}
