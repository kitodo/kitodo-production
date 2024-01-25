/**
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

import WaveSurfer from './../libs/wavesurfer/wavesurfer.esm.js.jsf';

class AudioWaveform {
    #audioElement;
    #wavesurfer;
    #buildTimeout;
    #loader;
    #peaksCache = [];

    constructor() {
        this.init();
    }

    init() {
        let self = this;
        this.#audioElement = document.querySelector('audio.mediaPreviewItem');
        if (this.#audioElement && this.#audioElement.getAttribute("data-audio-waveform") !== "initialized") {
            this.#audioElement.setAttribute("data-audio-waveform", "initialized");

            // add a loader to visualize loading process
            this.#loader = document.createElement("div");
            this.#loader.innerHTML = '<i class="fa fa-spinner fa-spin"/>';
            this.#loader.classList.add('loader');
            this.#audioElement.parentNode.insertBefore(this.#loader, this.#audioElement);

            // when the user agent can play the media
            this.#audioElement && this.#audioElement.addEventListener("canplay", () => {
                // Prevent browser crashes during audio decoding when multiple rapid clicks, such as double-clicking, occur.
                clearTimeout(this.#buildTimeout);
               self.#buildTimeout = setTimeout(function() {
                    self.#build();
               }, 500);
            }, {once: true});

        }
    }

    #build() {
        let self = this;
        // wavesurfer uses the 'src' attribute of the audio element, and we add this attribute based on the browser's current source selection
        this.#audioElement.src = this.#audioElement.currentSrc;

        // get the media id from the source parameter
        const urlParams = new URLSearchParams(this.#audioElement.src);
        let mediaId = urlParams.get('mediaId');

        let waveContainer = document.createElement("div");
        waveContainer.setAttribute("id", "wave-container");
        waveContainer.onclick = function () {
            self.#wavesurfer.playPause();
        };
        waveContainer.style.width = "90%";
        waveContainer.style.display = "none";
        this.#audioElement.parentNode.insertBefore(waveContainer, this.#audioElement);

        // add fixed width to prevent zooming overflow
        this.#audioElement.parentNode.style.width = this.#audioElement.parentNode.clientWidth + 'px';

        this.#wavesurfer = WaveSurfer.create({
            container: document.getElementById(waveContainer.getAttribute("id")),
            height: 250,
            waveColor: "#f3f3f3",
            progressColor: "#ff4e00",
            cursorColor: "#ffffff",
            media: this.#audioElement,
            minPxPerSec: 0,
            peaks: this.#peaksCache[mediaId]
        });

        this.#wavesurfer.on("decode", function () {
            // cache peaks after when audio has been decoded
            self.#peaksCache[mediaId] = self.#wavesurfer.getDecodedData().getChannelData(0);
        });

        this.#wavesurfer.on("ready", function () {
            waveContainer.style.display = "block";
            self.#loader.style.display = "none";

            let waveToolsContainer = document.getElementById("imagePreviewForm:audioWaveformTools");
            const waveToolsSlider = waveToolsContainer.querySelector('input[type="range"]');

            waveToolsSlider.addEventListener('input', (e) => {
                const minPxPerSec = e.target.valueAsNumber;
                self.#wavesurfer.zoom(minPxPerSec);
            });

            waveToolsContainer.querySelectorAll('input[type="checkbox"]').forEach((input) => {
                input.onchange = (e) => {
                    self.#wavesurfer.setOptions({
                        [input.value]: e.target.checked,
                    });
                };
            });
        });
    }
}

const audioWaveform= new AudioWaveform();

document.addEventListener("kitodo-metadataditor-mediaview-update", function () {
    audioWaveform.init();
});
