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

import WaveSurfer from './libs/wavesurfer/wavesurfer.esm.js.jsf'

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
        let self = this
        this.#audioElement = document.querySelector('audio.mediaPreviewItem')
        if (this.#audioElement.getAttribute("data-audio-waveform") != "initialized") {
            this.#audioElement.setAttribute("data-audio-waveform", "initialized")

            this.#audioElement && this.#audioElement.addEventListener("canplay", () => {
               clearTimeout(this.#buildTimeout );
               self.#buildTimeout = setTimeout(function() {
                    self.#build();
               }, 500)
            }, {once: true});

            this.#loader = document.createElement("div");
            this.#loader.innerHTML = '<i class="fa fa-spinner fa-spin"/>'
            this.#loader.classList.add('loader')
            this.#audioElement.parentNode.insertBefore(this.#loader, this.#audioElement);

        }
    }

    #build() {
        let self = this
        this.#audioElement.src = this.#audioElement.currentSrc;

        const urlParams = new URLSearchParams(this.#audioElement.src);
        let mediaId = urlParams.get('mediaId')

        let waveContainer = document.createElement("div");
        waveContainer.setAttribute("id", "wave-container");
        waveContainer.onclick = function () {
            self.#wavesurfer.playPause()
        }
        waveContainer.style.width = "90%";
        waveContainer.style.display = "none";
        this.#audioElement.parentNode.insertBefore(waveContainer, this.#audioElement);

        this.#wavesurfer = WaveSurfer.create({
            container: document.getElementById(waveContainer.getAttribute("id")),
            height: 100,
            waveColor: "#f3f3f3",
            progressColor: "#ff4e00",
            cursorColor: "#ffffff",
            media: this.#audioElement,
            minPxPerSec: 0,
            peaks: this.#peaksCache[mediaId]
        });

        console.log("add ready handler");

        // cache peaks after when audio has been decoded
        this.#wavesurfer.on("decode", function () {
            self.#peaksCache[mediaId] = self.#wavesurfer.getDecodedData().getChannelData(0)
        });

        this.#wavesurfer.on("ready", function () {
            console.log("run ready handler");
            waveContainer.style.display = "block";
            self.#loader.style.display = "none";

            let waveToolsContainer = document.getElementById("waveTools")
            const waveToolsSlider = waveToolsContainer.querySelector('input[type="range"]')

            waveToolsSlider.addEventListener('input', (e) => {
                const minPxPerSec = e.target.valueAsNumber
                self.#wavesurfer.zoom(minPxPerSec)
            })

            waveToolsContainer.querySelectorAll('input[type="checkbox"]').forEach((input) => {
                input.onchange = (e) => {
                    self.#wavesurfer.setOptions({
                        [input.value]: e.target.checked,
                    })
                }
            })
            const jumpButtons = document.getElementsByClassName("audio-waveform-jump-button");
            Array.from(jumpButtons).forEach(function (jumpButton) {
                jumpButton.addEventListener('click', function (event) {
                    event.stopPropagation();
                    let jumpSeconds = parseInt(this.getAttribute("data-audio-waveform-jump-seconds"));
                    self.#wavesurfer.setTime(self.#wavesurfer.getCurrentTime() + jumpSeconds)
                });
            });
        });

        this.#wavesurfer.on("error", function (e) {
            console.error(e);
        });
    }

}

console.log("first initialisation");
const audioWaveform= new AudioWaveform()

document.addEventListener("kitodo-metadataditor-mediaview-update", function () {
    console.log("update media view");
    audioWaveform.init();
});
