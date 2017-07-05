package org.vitrivr.cineast.core.features.exporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.Spectrum;
import org.vitrivr.cineast.core.util.dsp.visualization.AudioSignalVisualizer;
import org.vitrivr.cineast.core.util.dsp.fft.windows.HanningWindow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * Visualizes and exporst the power spectogram (time vs. frequency vs. power) of the provided
 * AudioSegment.
 *
 * @author rgasser
 * @version 1.0
 * @created 31.01.17
 */
public class AudioSpectogramExporter implements Extractor {


    private static final Logger LOGGER = LogManager.getLogger();

    /** Property names that can be used in the configuration hash map. */
    private static final String PROPERTY_NAME_DESTINATION = "destination";
    private static final String PROPERTY_NAME_WIDTH = "width";
    private static final String PROPERTY_NAME_HEIGHT = "height";

    /** Destination path; can be set in the AudioWaveformExporter properties. */
    private Path destination = Paths.get(Config.sharedConfig().getExtractor().getOutputLocation().toString());

    /** Width of the resulting image in pixels. */
    private int width = 800;

    /** Height of the resulting image in pixels. */
    private int height = 600;


    /**
     * Default constructor. The AudioWaveformExporter can be configured via named properties
     * in the provided HashMap. Supported parameters:
     *
     * <ol>
     *      <li>destination: Path where images should be stored.</li>
     *      <li>width: Width of the image in pixels.</li>
     *      <li>height: Height of the image in pixels.</li>
     * </ol>
     *
     * @param properties HashMap containing named properties
     */
    public AudioSpectogramExporter(HashMap<String, String> properties) {
        if (properties.containsKey(PROPERTY_NAME_DESTINATION)) {
            this.destination = Paths.get(properties.get(PROPERTY_NAME_DESTINATION));
        }

        if (properties.containsKey(PROPERTY_NAME_WIDTH)) {
            this.width = Integer.parseInt(properties.get(PROPERTY_NAME_WIDTH));
        }

        if (properties.containsKey(PROPERTY_NAME_HEIGHT)) {
            this.height = Integer.parseInt(properties.get(PROPERTY_NAME_HEIGHT));
        }
    }

    @Override
    public void processShot(SegmentContainer shot) {
        /* IF shot has no samples, this step is skipped. */
        if (shot.getNumberOfSamples() == 0) return;

        /* Prepare STFT and Spectrum for the segment. */
        final Path directory = this.destination.resolve(shot.getSuperId());
        final STFT stft = shot.getSTFT(2048, 512, new HanningWindow());
        final List<Spectrum> spectrums = stft.getPowerSpectrum();

        /* Visualize Spectrum and write it to disc. */
        try {
            BufferedImage image = AudioSignalVisualizer.visualizeSpectogram(spectrums, this.width, this.height);
            if (image != null) {
                Files.createDirectories(directory);
                ImageIO.write(image, "JPEG", directory.resolve(shot.getId() + ".jpg").toFile());
            } else {
                LOGGER.warn("Spectrum could not be visualized!");
            }
        } catch (IOException exception) {
            LOGGER.error("A serious error occurred while writing the spectrum image! ({})", LogHelper.getStackTrace(exception));
        }
    }

    @Override
    public void init(PersistencyWriterSupplier phandlerSupply) { /* Noting to init. */}

    @Override
    public void finish() { /* Nothing to finish. */}

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {/* Nothing to initialize. */}

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {/* Nothing to drop. */}
}
