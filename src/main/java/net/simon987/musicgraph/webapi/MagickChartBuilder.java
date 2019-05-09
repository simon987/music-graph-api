package net.simon987.musicgraph.webapi;

import net.simon987.musicgraph.entities.ReleaseDetails;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MagickChartBuilder extends AbstractBinder implements IChartBuilder {

    private String workspacePath;
    private File workspace;

    private static final String[] letters = new String[]{
            "A", "B", "C", "D", "E", "F", "G", "H", "I"
    };

    @Override
    protected void configure() {
        bind(this).to(IChartBuilder.class);
    }

    public MagickChartBuilder(String workspacePath) {
        this.workspacePath = workspacePath;
        this.workspace = new File(workspacePath);
    }

    @Override
    public byte[] makeChart(ChartOptions options) throws Exception {

        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> releases = options.details.stream().map(o -> o.mbid).collect(Collectors.toList());

        File chartWorkspace = makeChartWorkspace();

        saveCoversToDisk(options, chartWorkspace);
        makeGrid(options, chartWorkspace, releases);
        File header = makeHeader(options, chartWorkspace);
        File side = makeSide(options, chartWorkspace);

        // Side + grid
        processBuilder
                .directory(chartWorkspace)
                .command(
                        "convert",
                        "+append",
                        side.toString(),
                        "grid.png",
                        "grid_with_side.png"
                )
                .start()
                .waitFor();

        // Side + grid
        processBuilder
                .directory(chartWorkspace)
                .command(
                        "convert",
                        "-append",
                        "-gravity", "east",
                        header.toString(),
                        "grid_with_side.png",
                        "final_grid.png"
                )
                .start()
                .waitFor();

        makeDescription(options, chartWorkspace);

        // + description
        processBuilder
                .directory(chartWorkspace)
                .command(
                        "convert",
                        "+append",
                        "-gravity", "south",
                        "final_grid.png",
                        "description.png",
                        "final.png"
                )
                .start()
                .waitFor();


        FileInputStream file = new FileInputStream(new File(chartWorkspace, "final.png"));
        byte[] chart = file.readAllBytes();
        file.close();

        cleanup(chartWorkspace);

        return chart;
    }

    private void cleanup(File chartWorkspace) {
        String[] entries = chartWorkspace.list();
        if (entries != null) {
            for (String s : entries) {
                File currentFile = new File(chartWorkspace, s);
                currentFile.delete();
            }
        }
        chartWorkspace.delete();
    }

    private File makeChartWorkspace() throws IOException {
        String chartId = UUID.randomUUID().toString();

        File chartWorkspace = new File(workspacePath, chartId);
        if (!chartWorkspace.mkdir()) {
            throw new IOException(String.format("Could not create chart workspace: %s", chartWorkspace.getAbsolutePath()));
        }
        return chartWorkspace;
    }

    private void saveCoversToDisk(ChartOptions options, File chartWorkspace) throws IOException {
        for (ReleaseDetails releaseDetails : options.details) {
            if (options.covers.containsKey(releaseDetails.mbid)) {
                FileOutputStream file = new FileOutputStream(new File(chartWorkspace, releaseDetails.mbid));
                file.write(options.covers.get(releaseDetails.mbid));
                file.close();
            }
        }
    }

    private void makeDescription(ChartOptions options, File chartWorkspace)
            throws InterruptedException, IOException {

        ProcessBuilder processBuilder = new ProcessBuilder();

        for (int i = 0; i < options.rows; i++) {

            String desc = formatDescription(options, i);

            processBuilder
                    .directory(chartWorkspace)
                    .command(
                            "convert",
                            "-size", "620x316",
                            "xc:" + options.backgroundColor,
                            "-fill", options.textColor,
                            "-pointsize", "20",
                            "-font", options.font,
                            "-annotate", "+0+40", desc,
                            String.format("d_%d.png", i)
                    )
                    .start()
                    .waitFor();
            processBuilder
                    .directory(chartWorkspace)
                    .command(
                            "convert",
                            "-append",
                            "d_*.png",
                            "description.png"
                    )
                    .start()
                    .waitFor();
        }
    }

    private static String formatDescription(ChartOptions options, int row) {

        StringBuilder sb = new StringBuilder();

        for (int col = 0; col < options.cols; col++) {

            int releaseIndex = row * options.cols + col;
            if (releaseIndex >= options.details.size()) {
                break;
            }

            ReleaseDetails release = options.details.get(releaseIndex);

            String year = release.year != 0 ? String.format("(%s)", release.year) : "";

            if (release.name.length() + release.artist.length() < 40) {
                sb.append(String.format("%s%d %s - %s %s\n",
                        letters[row], col+1,
                        release.name, release.artist, year));
            } else {
                sb.append(String.format("%s%d %s -\n\t\t\t %s %s\n",
                        letters[row], col+1,
                        release.name, release.artist, year));
            }
        }
        return sb.toString();
    }

    private void makeGrid(ChartOptions options, File chartWorkspace, List<String> releases)
            throws InterruptedException, IOException {

        ProcessBuilder processBuilder = new ProcessBuilder();

        List<String> cmd = new ArrayList<>();
        cmd.add("montage");
        cmd.addAll(releases);
        cmd.add("-tile");
        cmd.add(String.format("%dx%d", options.cols, options.rows));
        cmd.add("-background");
        cmd.add(options.backgroundColor);
        cmd.add("-border");
        cmd.add("1");
        cmd.add("-bordercolor");
        cmd.add(options.borderColor);
        cmd.add("-geometry");
        cmd.add("256x256+30+30");
        cmd.add("grid.png");

        processBuilder
                .directory(chartWorkspace)
                .command(cmd)
                .start()
                .waitFor();
    }

    private File makeSide(ChartOptions options, File chartWorkSpace)
            throws InterruptedException, IOException {

        File sideFile = new File(chartWorkSpace, "side.png");

        ProcessBuilder processBuilder = new ProcessBuilder();

        for (int i = 0; i < options.rows; i++) {
            processBuilder
                    .directory(chartWorkSpace)
                    .command(
                            "convert",
                            "-background", options.backgroundColor,
                            "-gravity", "east",
                            "-size", "60x256",
                            "-fill", options.textColor,
                            "-pointsize", "64",
                            "-font", options.font,
                            "-strip",
                            String.format("label:%s", letters[i]),
                            String.format("s_%d.png", i + 1)
                    )
                    .start()
                    .waitFor();
        }

        processBuilder
                .directory(chartWorkSpace)
                .command(
                        "montage",
                        "-tile", "1x",
                        "-background", options.backgroundColor,
                        "-geometry", "+0+31",
                        "s_*.png",
                        sideFile.toString()
                )
                .start()
                .waitFor();

        return sideFile;
    }

    private File makeHeader(ChartOptions options, File chartWorkspace)
            throws InterruptedException, IOException {

        File headerFile = new File(chartWorkspace, "header.png");
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Header
        for (int i = 1; i <= options.cols; i++) {
            processBuilder
                    .directory(chartWorkspace)
                    .command(
                            "convert",
                            "-background", options.backgroundColor,
                            "-gravity", "south",
                            "-size", "256x80",
                            "-fill", options.textColor,
                            "-chop", "0x12",
                            "-pointsize", "64",
                            "-font", options.font,
                            "-strip",
                            String.format("label:%d", i),
                            String.format("h_%d.png", i)
                    )
                    .start()
                    .waitFor();
        }

        processBuilder
                .directory(chartWorkspace)
                .command(
                        "montage",
                        "-tile", "x1",
                        "h_*.png",
                        "-background", options.backgroundColor,
                        "-geometry", "+31+0",
                        headerFile.toString()
                )
                .start()
                .waitFor();

        return headerFile;
    }
}
