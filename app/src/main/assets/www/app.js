// Sekme Değiştirme
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
        btn.classList.add('active');
        document.getElementById('tab-' + btn.dataset.tab).classList.add('active');
    });
});

// Minecraft Başlat Düğmesi
document.getElementById('btnLaunchMC').addEventListener('click', () => {
    if (window.AndroidBridge && window.AndroidBridge.launchMinecraft) {
        window.AndroidBridge.launchMinecraft();
    } else {
        alert("Minecraft'ı başlatmak için uygulamayı Android telefonda açmalısınız.");
    }
});

// Yerel Depolama & Paket Yönetimi
let packs = JSON.parse(localStorage.getItem('mc_packs') || '[]');

function savePacks() {
    localStorage.setItem('mc_packs', JSON.stringify(packs));
    renderPacks();
}

function renderPacks() {
    const modList = document.getElementById('modList');
    const textureList = document.getElementById('textureList');
    const skinList = document.getElementById('skinList');

    modList.innerHTML = '';
    textureList.innerHTML = '';
    skinList.innerHTML = '';

    packs.forEach((pack, index) => {
        const card = document.createElement('div');
        card.className = 'pack-card';

        let iconContent = '📦';
        if (pack.type === 'texture') iconContent = '🎨';
        if (pack.type === 'skin') iconContent = pack.dataUrl ? `<img src="${pack.dataUrl}" class="pack-icon">` : '👤';

        card.innerHTML = `
            ${typeof iconContent === 'string' && iconContent.startsWith('<img') ? iconContent : `<div class="pack-icon">${iconContent}</div>`}
            <div class="pack-title">${pack.name}</div>
            <div class="pack-actions">
                <button class="btn-card btn-import" onclick="importPack(${index})">🚀 Aktar</button>
                <button class="btn-card btn-delete" onclick="deletePack(${index})">🗑️</button>
            </div>
        `;

        if (pack.type === 'mod') modList.appendChild(card);
        else if (pack.type === 'texture') textureList.appendChild(card);
        else if (pack.type === 'skin') skinList.appendChild(card);
    });
}

function handleFileUpload(input, type) {
    const file = input.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(e) {
        const base64Data = e.target.result.split(',')[1];
        const dataUrl = e.target.result;

        packs.push({
            id: Date.now(),
            name: file.name,
            type: type,
            base64: base64Data,
            dataUrl: type === 'skin' ? dataUrl : null
        });

        savePacks();
        if (window.AndroidBridge && window.AndroidBridge.showToast) {
            window.AndroidBridge.showToast(file.name + " eklendi!");
        }
        input.value = '';
    };
    reader.readAsDataURL(file);
}

document.getElementById('inputMod').addEventListener('change', (e) => handleFileUpload(e.target, 'mod'));
document.getElementById('inputTexture').addEventListener('change', (e) => handleFileUpload(e.target, 'texture'));
document.getElementById('inputSkin').addEventListener('change', (e) => handleFileUpload(e.target, 'skin'));

function importPack(index) {
    const pack = packs[index];
    if (window.AndroidBridge && window.AndroidBridge.sendToMinecraft) {
        window.AndroidBridge.sendToMinecraft(pack.name, pack.base64);
    } else {
        alert("Minecraft'a aktarma özelliği yalnız uygulamada çalışır.");
    }
}

function deletePack(index) {
    packs.splice(index, 1);
    savePacks();
}

// Başlangıçta yükle
renderPacks();
