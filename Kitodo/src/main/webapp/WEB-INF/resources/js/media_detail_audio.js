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

import WaveSurfer from 'https://unpkg.com/wavesurfer.js@beta'

let audios = document.querySelectorAll('audio.mediaPreviewItem');

let audio = audios[0];
audio.src = audio.currentSrc;

let waveTools = document.createElement("div");
waveTools.setAttribute("id", "wave-tools");
waveTools.style.width = "90%";
waveTools.innerHTML = "<label>Zoom: <input type=\"range\" min=\"10\" max=\"1000\" value=\"0\"></label><label><input type=\"checkbox\" checked=\"\" value=\"autoCenter\"> Auto center</label>";
audio.parentNode.insertBefore(waveTools, audio);

let waveContainer = document.createElement("div");
waveContainer.setAttribute("id", "wave-container");
waveContainer.onclick = function(){wavesurfer.playPause()}
waveContainer.style.width = "90%";
audio.parentNode.insertBefore(waveContainer, audio);

const wavesurfer = WaveSurfer.create({
    container: document.getElementById(waveContainer.getAttribute("id")),
    height: 100,
    waveColor: "#f3f3f3",
    progressColor: "#ff4e00",
    cursorColor: "#ffffff",
    media: audio,
    minPxPerSec: 10,
});

wavesurfer.once('decode', () => {
    let waveToolsContainer = document.getElementById(waveTools.getAttribute("id"))
    const waveToolsSlider = waveToolsContainer.querySelector('input[type="range"]')

    waveToolsSlider.addEventListener('input', (e) => {
        const minPxPerSec = e.target.valueAsNumber
        wavesurfer.zoom(minPxPerSec)
    })

    waveToolsContainer.querySelectorAll('input[type="checkbox"]').forEach((input) => {
        input.onchange = (e) => {
            wavesurfer.setOptions({
                [input.value]: e.target.checked,
            })
        }
    })
})


wavesurfer.on("error", function (e) {
    console.warn(e);
});

// Load audio from URL
//wavesurfer.load(audio);
