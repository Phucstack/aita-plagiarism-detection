/**
 * AITA CodeDefend 2026 - 3D Cyber Security Shield & Quantum Scanner Engine (Studio PBR v6 - Ultra HUD Edition)
 * - Absolute Mathematical Concentric Alignment: Lock emblem centered at (0, 0, 0)
 * - 4-Axis Holographic Crosshair Rotor & 360° Radar Sweep Fan
 * - Dual Counter-Rotating Caliper Tech Arcs with Pulsing Quantum Nodes
 * - Precision Sci-Fi Degree Dial with 12 Angular Tick Marks
 * - 3D Gyroscopic Gimbal Rings with 4 High-Glow Orbiting Photon Beacons
 * - Biometric Dual-Beam Laser Scanner with Cyber Brackets & Soft Curtain
 * - Studio Multi-Light PBR Rig (White Key Light 2.8, Cyan Fill 2.2, Violet Rim 2.6)
 * - 4-Stage State Machine with Golden Ratio Proportions
 */
(function (global) {
  let scene, camera, renderer, shieldGroup;
  let shieldMesh, innerCore;
  let glbWrapper = null, glbModel = null, glbBaseScale = 1.0;

  // Ultra Sci-Fi HUD & Radar Rotor Systems
  let radarRotorGroup, rotorSpineMat, rotorCrossMat, rotorHubMat, rotorCaliperMat, rotorFanMesh, rotorFanMat;
  let techArcsGroup1, techArcsGroup2, arcsMat1, arcsMat2, arcsDotsMat;
  let reticleGroup, reticleMat, reticleDialMat;
  let gyroRing1, gyroRing2, gyroMat1, gyroMat2;
  let gyroBeacons = []; // 4 orbiting quantum photon nodes
  let laserScannerGroup, laserBarMat, laserBracketMat, curtainMat, curtainMesh;
  let particleSystem, coreLight, whiteKeyLight, cyanFillLight, rimLight;

  let animId = null;
  let currentStage = 0; // 0: Idle, 1: SHA-256, 2: AST, 3: Gemini, 4: Breach Flagged
  let targetRotationX = 0, targetRotationY = 0;
  let laserY = 0, laserDirection = 1;
  let isDragging = false, prevMouse = { x: 0, y: 0 };

  const STAGE_CONFIGS = {
    0: { // Idle (Cyber Cyan & Indigo)
      shieldColor: 0x06b6d4,
      emissiveColor: 0x083344,
      coreColor: 0x22d3ee,
      rotorColor: 0x06b6d4,
      rotorFanColor: 0x0891b2,
      arcsColor1: 0x06b6d4,
      arcsColor2: 0x8b5cf6,
      reticleColor: 0x0891b2,
      beaconColor: 0x22d3ee,
      laserColor: 0x06b6d4,
      rotationSpeed: 0.010,
      laserSpeed: 0.032,
      alarmPulse: false
    },
    1: { // SHA-256 Hash Verification (Emerald Matrix)
      shieldColor: 0x10b981,
      emissiveColor: 0x064e3b,
      coreColor: 0x34d399,
      rotorColor: 0x10b981,
      rotorFanColor: 0x059669,
      arcsColor1: 0x10b981,
      arcsColor2: 0x06b6d4,
      reticleColor: 0x059669,
      beaconColor: 0x34d399,
      laserColor: 0x10b981,
      rotationSpeed: 0.018,
      laserSpeed: 0.065,
      alarmPulse: false
    },
    2: { // AST Syntax Parsing (Amber Warning Core)
      shieldColor: 0xf59e0b,
      emissiveColor: 0x78350f,
      coreColor: 0xfbbf24,
      rotorColor: 0xf59e0b,
      rotorFanColor: 0xd97706,
      arcsColor1: 0xf59e0b,
      arcsColor2: 0xf97316,
      reticleColor: 0xd97706,
      beaconColor: 0xfbbf24,
      laserColor: 0xfbbf24,
      rotationSpeed: 0.024,
      laserSpeed: 0.080,
      alarmPulse: false
    },
    3: { // Gemini 1.5 Pro Semantic Vector Embedding (Quantum Violet)
      shieldColor: 0x8b5cf6,
      emissiveColor: 0x4c1d95,
      coreColor: 0xa78bfa,
      rotorColor: 0xa78bfa,
      rotorFanColor: 0x7c3aed,
      arcsColor1: 0x8b5cf6,
      arcsColor2: 0x06b6d4,
      reticleColor: 0x7c3aed,
      beaconColor: 0xc084fc,
      laserColor: 0xc084fc,
      rotationSpeed: 0.030,
      laserSpeed: 0.095,
      alarmPulse: false
    },
    4: { // 88.5% Plagiarism Breach Flagged! (Crimson Danger Alert)
      shieldColor: 0xef4444,
      emissiveColor: 0x7f1d1d,
      coreColor: 0xf87171,
      rotorColor: 0xff3333,
      rotorFanColor: 0xb91c1c,
      arcsColor1: 0xef4444,
      arcsColor2: 0xf43f5e,
      reticleColor: 0xb91c1c,
      beaconColor: 0xff6666,
      laserColor: 0xff2222,
      rotationSpeed: 0.038,
      laserSpeed: 0.125,
      alarmPulse: true
    }
  };

  function createFallbackGeometry() {
    const shape = new THREE.Shape();
    shape.moveTo(-1.6, 1.6);
    shape.lineTo(1.6, 1.6);
    shape.lineTo(1.9, 0.5);
    shape.lineTo(1.3, -1.0);
    shape.lineTo(0, -2.1);
    shape.lineTo(-1.3, -1.0);
    shape.lineTo(-1.9, 0.5);
    shape.closePath();

    const extrudeSettings = {
      depth: 0.3,
      bevelEnabled: true,
      bevelSegments: 4,
      steps: 1,
      bevelSize: 0.14,
      bevelThickness: 0.14
    };

    return new THREE.ExtrudeGeometry(shape, extrudeSettings);
  }

  // Soft Falloff Laser Curtain Texture
  function createLaserCurtainTexture() {
    const canvas = document.createElement('canvas');
    canvas.width = 256;
    canvas.height = 128;
    const ctx = canvas.getContext('2d');

    const vGrad = ctx.createLinearGradient(0, 128, 0, 0);
    vGrad.addColorStop(0, 'rgba(255, 255, 255, 0.95)');
    vGrad.addColorStop(0.12, 'rgba(6, 182, 212, 0.8)');
    vGrad.addColorStop(0.5, 'rgba(6, 182, 212, 0.25)');
    vGrad.addColorStop(1, 'rgba(6, 182, 212, 0.0)');
    ctx.fillStyle = vGrad;
    ctx.fillRect(0, 0, 256, 128);

    const hGrad = ctx.createLinearGradient(0, 0, 256, 0);
    hGrad.addColorStop(0, 'rgba(0,0,0,1)');
    hGrad.addColorStop(0.15, 'rgba(0,0,0,0)');
    hGrad.addColorStop(0.85, 'rgba(0,0,0,0)');
    hGrad.addColorStop(1, 'rgba(0,0,0,1)');
    ctx.globalCompositeOperation = 'destination-out';
    ctx.fillStyle = hGrad;
    ctx.fillRect(0, 0, 256, 128);

    const texture = new THREE.CanvasTexture(canvas);
    texture.needsUpdate = true;
    return texture;
  }

  // Radar Hologram Sweep Fan Texture (Ultra smooth Conic Wedge Gradient)
  function createRadarSweepTexture() {
    const canvas = document.createElement('canvas');
    canvas.width = 256;
    canvas.height = 256;
    const ctx = canvas.getContext('2d');
    const cx = 128, cy = 128, r = 124;

    const steps = 42;
    for (let i = 0; i < steps; i++) {
      const startAngle = (i / steps) * (Math.PI / 3.0);
      const endAngle = ((i + 1) / steps) * (Math.PI / 3.0);
      const alpha = Math.pow(i / steps, 1.8) * 0.65;
      ctx.beginPath();
      ctx.moveTo(cx, cy);
      ctx.arc(cx, cy, r, startAngle, endAngle);
      ctx.closePath();
      ctx.fillStyle = `rgba(6, 182, 212, ${alpha})`;
      ctx.fill();
    }

    const texture = new THREE.CanvasTexture(canvas);
    texture.needsUpdate = true;
    return texture;
  }

  function initCyberShield3D(containerId = 'batch-shield-3d-viewport') {
    const container = document.getElementById(containerId);
    if (!container || typeof THREE === 'undefined') return;

    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      return;
    }

    const width = container.clientWidth || 340;
    const height = container.clientHeight || 240;

    while (container.firstChild) {
      container.removeChild(container.firstChild);
    }

    scene = new THREE.Scene();
    camera = new THREE.PerspectiveCamera(38, width / height, 0.1, 100);
    camera.position.set(0, 0, 8.8);

    renderer = new THREE.WebGLRenderer({
      alpha: true,
      antialias: true,
      powerPreference: 'high-performance'
    });
    renderer.setSize(width, height);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 1.45;
    container.appendChild(renderer.domElement);

    // Studio Multi-Light PBR Rig (Neutral White Key + Soft Rim for authentic titanium look)
    const ambientLight = new THREE.AmbientLight(0xffffff, 1.45);
    scene.add(ambientLight);

    whiteKeyLight = new THREE.DirectionalLight(0xffffff, 2.5);
    whiteKeyLight.position.set(3.0, 4.5, 6.0);
    scene.add(whiteKeyLight);

    cyanFillLight = new THREE.DirectionalLight(0xe2e8f0, 1.2);
    cyanFillLight.position.set(-4.5, 2.0, 4.0);
    scene.add(cyanFillLight);

    rimLight = new THREE.DirectionalLight(0xc4b5fd, 1.3);
    rimLight.position.set(4.0, -2.5, -4.0);
    scene.add(rimLight);

    coreLight = new THREE.PointLight(0xffffff, 0.9, 10);
    coreLight.position.set(0, 0, 2.2);
    scene.add(coreLight);

    shieldGroup = new THREE.Group();
    scene.add(shieldGroup);

    // Model Wrapper Group: Keeps all 3D rotations pivoting precisely around (0,0,0)
    glbWrapper = new THREE.Group();
    shieldGroup.add(glbWrapper);

    // 1. Procedural Fallback Shield Mesh (Calibrated center at (0,0,0))
    const fallbackGeo = createFallbackGeometry();
    fallbackGeo.center();
    const fallbackMat = new THREE.MeshPhysicalMaterial({
      color: 0x06b6d4,
      emissive: 0x083344,
      emissiveIntensity: 0.6,
      metalness: 0.85,
      roughness: 0.2,
      transmission: 0.35,
      transparent: true,
      opacity: 0.92,
      clearcoat: 1.0,
      clearcoatRoughness: 0.1
    });
    shieldMesh = new THREE.Mesh(fallbackGeo, fallbackMat);
    shieldMesh.position.set(0, -0.34, 0); // Aligns emblem at exact (0,0,0)
    glbWrapper.add(shieldMesh);

    // 2. Inner Spinning Quantum AST Core
    const coreGeo = new THREE.IcosahedronGeometry(0.48, 1);
    const coreMat = new THREE.MeshBasicMaterial({
      color: 0x22d3ee,
      wireframe: true,
      transparent: true,
      opacity: 0.8
    });
    innerCore = new THREE.Mesh(coreGeo, coreMat);
    shieldGroup.add(innerCore);

    // =========================================================================
    // 3. ULTRA HUD COMPONENT 1: 4-AXIS HOLOGRAPHIC ROTOR & 360° RADAR SWEEP
    // Sits at (0, 0, 0.40) - True concentric pivot through lock emblem
    // =========================================================================
    radarRotorGroup = new THREE.Group();
    radarRotorGroup.position.set(0, 0, 0.40);

    // 3a. Primary Long Rotor Spine (Length 3.5)
    const rotorSpineGeo = new THREE.CylinderGeometry(0.018, 0.018, 3.5, 16);
    rotorSpineMat = new THREE.MeshBasicMaterial({
      color: 0x06b6d4,
      transparent: true,
      opacity: 0.95
    });
    const rotorSpine = new THREE.Mesh(rotorSpineGeo, rotorSpineMat);
    radarRotorGroup.add(rotorSpine);

    // 3b. Secondary Crosshair Spine (Length 1.6, perpendicular)
    const rotorCrossGeo = new THREE.CylinderGeometry(0.012, 0.012, 1.6, 16);
    rotorCrossMat = new THREE.MeshBasicMaterial({
      color: 0x22d3ee,
      transparent: true,
      opacity: 0.85
    });
    const rotorCross = new THREE.Mesh(rotorCrossGeo, rotorCrossMat);
    rotorCross.rotation.z = Math.PI / 2;
    radarRotorGroup.add(rotorCross);

    // 3c. Center Hub Reticle Ring (Encircling the Lock Emblem at (0,0,0))
    const rotorHubGeo = new THREE.TorusGeometry(0.38, 0.024, 16, 48);
    rotorHubMat = new THREE.MeshBasicMaterial({
      color: 0x22d3ee,
      transparent: true,
      opacity: 0.95
    });
    const rotorHub = new THREE.Mesh(rotorHubGeo, rotorHubMat);
    radarRotorGroup.add(rotorHub);

    // 4 Mini Reticle Corner Clamps at Hub (0, 90, 180, 270 deg)
    const clampGeo = new THREE.BoxGeometry(0.05, 0.10, 0.04);
    rotorCaliperMat = new THREE.MeshBasicMaterial({
      color: 0x06b6d4,
      transparent: true,
      opacity: 0.95
    });
    for (let c = 0; c < 4; c++) {
      const clamp = new THREE.Mesh(clampGeo, rotorCaliperMat);
      const ang = (c * Math.PI) / 2;
      clamp.position.set(0.38 * Math.cos(ang), 0.38 * Math.sin(ang), 0);
      clamp.rotation.z = ang;
      radarRotorGroup.add(clamp);
    }

    // 3d. Dual Caliper Emitter Tips at extremities (-1.75 and +1.75)
    const tipGeo = new THREE.BoxGeometry(0.09, 0.18, 0.06);
    const tipTop = new THREE.Mesh(tipGeo, rotorCaliperMat);
    tipTop.position.set(0, 1.75, 0);
    radarRotorGroup.add(tipTop);

    const tipBottom = tipTop.clone();
    tipBottom.position.set(0, -1.75, 0);
    radarRotorGroup.add(tipBottom);

    // 3e. 360° Radar Hologram Sweep Fan (Fan Wedge with smooth gradient falloff)
    const sweepTexture = createRadarSweepTexture();
    const sweepGeo = new THREE.CircleGeometry(1.85, 36, 0, Math.PI / 3.0);
    rotorFanMat = new THREE.MeshBasicMaterial({
      map: sweepTexture,
      transparent: true,
      opacity: 0.22,
      blending: THREE.AdditiveBlending,
      side: THREE.DoubleSide,
      depthWrite: false
    });
    rotorFanMesh = new THREE.Mesh(sweepGeo, rotorFanMat);
    rotorFanMesh.rotation.z = -Math.PI / 3.0; // Anchored flush against rotor spine
    radarRotorGroup.add(rotorFanMesh);

    shieldGroup.add(radarRotorGroup);

    // =========================================================================
    // 4. ULTRA HUD COMPONENT 2: DUAL COUNTER-ROTATING TECH ARCS
    // =========================================================================
    // Inner Tech Arcs (Radius 1.95, 3 segmented arcs with terminal diodes)
    techArcsGroup1 = new THREE.Group();
    techArcsGroup1.position.set(0, 0, 0.20);
    arcsMat1 = new THREE.MeshBasicMaterial({
      color: 0x06b6d4,
      transparent: true,
      opacity: 0.92
    });
    arcsDotsMat = new THREE.MeshBasicMaterial({
      color: 0x22d3ee,
      transparent: true,
      opacity: 0.95
    });

    const arcSegGeo1 = new THREE.TorusGeometry(1.95, 0.028, 12, 36, Math.PI * 0.44);
    for (let i = 0; i < 3; i++) {
      const arcMesh = new THREE.Mesh(arcSegGeo1, arcsMat1);
      arcMesh.rotation.z = i * ((Math.PI * 2) / 3);
      techArcsGroup1.add(arcMesh);

      // Quantum diode at arc head
      const dotGeo = new THREE.SphereGeometry(0.05, 8, 8);
      const dotMesh = new THREE.Mesh(dotGeo, arcsDotsMat);
      const angle = i * ((Math.PI * 2) / 3);
      dotMesh.position.set(1.95 * Math.cos(angle), 1.95 * Math.sin(angle), 0);
      techArcsGroup1.add(dotMesh);
    }
    shieldGroup.add(techArcsGroup1);

    // Outer Tech Arcs (Radius 2.28, 4 segmented arcs, counter-rotating)
    techArcsGroup2 = new THREE.Group();
    techArcsGroup2.position.set(0, 0, 0.12);
    arcsMat2 = new THREE.MeshBasicMaterial({
      color: 0x8b5cf6,
      transparent: true,
      opacity: 0.85
    });

    const arcSegGeo2 = new THREE.TorusGeometry(2.28, 0.022, 12, 36, Math.PI * 0.28);
    for (let i = 0; i < 4; i++) {
      const arcMesh2 = new THREE.Mesh(arcSegGeo2, arcsMat2);
      arcMesh2.rotation.z = i * (Math.PI / 2) + 0.25;
      techArcsGroup2.add(arcMesh2);
    }
    shieldGroup.add(techArcsGroup2);

    // =========================================================================
    // 5. ULTRA HUD COMPONENT 3: PRECISION DEGREE DIAL WITH 12 TICKS
    // =========================================================================
    reticleGroup = new THREE.Group();
    reticleGroup.position.set(0, 0, 0.06);
    reticleDialMat = new THREE.MeshBasicMaterial({
      color: 0x0891b2,
      transparent: true,
      opacity: 0.65
    });
    reticleMat = new THREE.MeshBasicMaterial({
      color: 0x22d3ee,
      transparent: true,
      opacity: 0.85
    });

    const reticleRingGeo = new THREE.TorusGeometry(2.10, 0.010, 12, 64);
    const reticleRing = new THREE.Mesh(reticleRingGeo, reticleDialMat);
    reticleGroup.add(reticleRing);

    // 12 Angular Tick Marks around circle (Major at 90 deg, Minor at 30 deg)
    for (let i = 0; i < 12; i++) {
      const isMajor = (i % 3 === 0);
      const tickGeo = new THREE.BoxGeometry(0.02, isMajor ? 0.16 : 0.09, 0.015);
      const tick = new THREE.Mesh(tickGeo, isMajor ? reticleMat : reticleDialMat);
      const ang = (i * Math.PI) / 6;
      tick.position.set(2.10 * Math.cos(ang), 2.10 * Math.sin(ang), 0);
      tick.rotation.z = ang + Math.PI / 2;
      reticleGroup.add(tick);
    }
    shieldGroup.add(reticleGroup);

    // =========================================================================
    // 6. ULTRA HUD COMPONENT 4: 3D GYROSCOPE GIMBAL WITH 4 PHOTON BEACONS
    // Balanced concentric circular rings with smooth 3D tilting
    // =========================================================================
    const gyroRingGeo1 = new THREE.TorusGeometry(2.05, 0.015, 16, 96);
    gyroMat1 = new THREE.MeshBasicMaterial({
      color: 0x06b6d4,
      transparent: true,
      opacity: 0.85
    });
    gyroRing1 = new THREE.Mesh(gyroRingGeo1, gyroMat1);
    gyroRing1.rotation.x = Math.PI / 2.6;
    gyroRing1.rotation.y = 0.25;
    shieldGroup.add(gyroRing1);

    const gyroRingGeo2 = new THREE.TorusGeometry(2.18, 0.015, 16, 96);
    gyroMat2 = new THREE.MeshBasicMaterial({
      color: 0x8b5cf6,
      transparent: true,
      opacity: 0.80
    });
    gyroRing2 = new THREE.Mesh(gyroRingGeo2, gyroMat2);
    gyroRing2.rotation.y = Math.PI / 2.5;
    gyroRing2.rotation.z = -0.30;
    shieldGroup.add(gyroRing2);

    // 4 High-Glow Orbiting Quantum Photon Beacons (Mounted directly to Gyro Rings)
    gyroBeacons = [];
    const beaconGeo = new THREE.SphereGeometry(0.065, 12, 12);
    const beaconMat = new THREE.MeshBasicMaterial({
      color: 0x22d3ee,
      transparent: true,
      opacity: 0.98
    });
    for (let i = 0; i < 4; i++) {
      const beacon = new THREE.Mesh(beaconGeo, beaconMat);
      if (i < 2) {
        gyroRing1.add(beacon);
      } else {
        gyroRing2.add(beacon);
      }
      gyroBeacons.push(beacon);
    }

    // =========================================================================
    // 7. ULTRA HUD COMPONENT 5: BIOMETRIC DUAL-BEAM LASER SCANNER ASSEMBLY
    // Sweeps symmetrically across emblem bounds (-1.65 to +1.65)
    // =========================================================================
    laserScannerGroup = new THREE.Group();

    // 7a. Primary Laser Beam Line
    const laserBarGeo = new THREE.CylinderGeometry(0.016, 0.016, 3.2, 16);
    laserBarMat = new THREE.MeshBasicMaterial({
      color: 0x06b6d4,
      transparent: true,
      opacity: 0.95
    });
    const laserBar = new THREE.Mesh(laserBarGeo, laserBarMat);
    laserBar.rotation.z = Math.PI / 2;
    laserScannerGroup.add(laserBar);

    // 7b. Left & Right Cyber Emitter Brackets
    laserBracketMat = new THREE.MeshBasicMaterial({
      color: 0x22d3ee,
      transparent: true,
      opacity: 0.95
    });
    const lBracketGeo = new THREE.BoxGeometry(0.08, 0.16, 0.06);
    const lBracket = new THREE.Mesh(lBracketGeo, laserBracketMat);
    lBracket.position.set(-1.6, 0, 0);
    laserScannerGroup.add(lBracket);

    const rBracket = lBracket.clone();
    rBracket.position.set(1.6, 0, 0);
    laserScannerGroup.add(rBracket);

    // 7c. Soft Volumetric Laser Curtain with Canvas Gradient Texture
    const laserTexture = createLaserCurtainTexture();
    const curtainGeo = new THREE.PlaneGeometry(3.2, 0.35);
    curtainMat = new THREE.MeshBasicMaterial({
      map: laserTexture,
      transparent: true,
      opacity: 0.22,
      blending: THREE.AdditiveBlending,
      side: THREE.DoubleSide,
      depthWrite: false
    });
    curtainMesh = new THREE.Mesh(curtainGeo, curtainMat);
    curtainMesh.position.y = 0.22;
    curtainMesh.position.z = 0.02;
    laserScannerGroup.add(curtainMesh);

    laserScannerGroup.position.z = 0.46;
    shieldGroup.add(laserScannerGroup);

    // 8. Orbital Background Security Particles
    const particleCount = 45;
    const particlePositions = new Float32Array(particleCount * 3);
    for (let i = 0; i < particleCount; i++) {
      const radius = 2.4 + Math.random() * 0.6;
      const theta = Math.random() * Math.PI * 2;
      const phi = (Math.random() - 0.5) * Math.PI;
      particlePositions[i * 3] = radius * Math.cos(theta) * Math.cos(phi);
      particlePositions[i * 3 + 1] = radius * Math.sin(phi);
      particlePositions[i * 3 + 2] = radius * Math.sin(theta) * Math.cos(phi);
    }
    const particleGeo = new THREE.BufferGeometry();
    particleGeo.setAttribute('position', new THREE.BufferAttribute(particlePositions, 3));
    const particleMat = new THREE.PointsMaterial({
      color: 0x06b6d4,
      size: 0.07,
      transparent: true,
      opacity: 0.75,
      blending: THREE.AdditiveBlending
    });
    particleSystem = new THREE.Points(particleGeo, particleMat);
    shieldGroup.add(particleSystem);

    // =========================================================================
    // 9. TRIPO3D GLB MODEL PIPELINE - ZERO-CORS BASE64 + OFFLINE + EXACT (0,0,0)
    // =========================================================================
    function base64ToArrayBuffer(base64) {
      const binary_string = window.atob(base64);
      const len = binary_string.length;
      const bytes = new Uint8Array(len);
      for (let i = 0; i < len; i++) {
        bytes[i] = binary_string.charCodeAt(i);
      }
      return bytes.buffer;
    }

    if (typeof THREE.GLTFLoader !== 'undefined') {
      const loader = new THREE.GLTFLoader();

      function setupGlbScene(gltf) {
        glbModel = gltf.scene;

        const rawBox = new THREE.Box3().setFromObject(glbModel);
        const rawCenter = rawBox.getCenter(new THREE.Vector3());
        const rawSize = rawBox.getSize(new THREE.Vector3());

        // Golden Ratio Scaling: Shield Height = 3.5 units
        const effectiveWidth = (rawSize.x <= rawSize.y && rawSize.x <= rawSize.z) ? rawSize.z : rawSize.x;
        const effectiveHeight = (rawSize.y <= rawSize.x && rawSize.y <= rawSize.z) ? rawSize.z : rawSize.y;
        const maxDim = Math.max(effectiveWidth, effectiveHeight, rawSize.z);
        if (maxDim > 0) {
          glbBaseScale = 3.5 / maxDim;
          glbWrapper.scale.set(glbBaseScale, glbBaseScale, glbBaseScale);
        }

        // MATHEMATICAL CONCENTRIC ALIGNMENT:
        // Căn tâm hoàn hảo hình học đối xứng đỉnh - đáy qua tọa độ (0, 0, 0)
        glbModel.position.set(-rawCenter.x, -rawCenter.y, -rawCenter.z);

        // Auto-orient broad face toward camera (+Z)
        if (rawSize.x <= rawSize.y && rawSize.x <= rawSize.z) {
          glbModel.rotation.y = Math.PI / 2;
        } else if (rawSize.y <= rawSize.x && rawSize.y <= rawSize.z) {
          glbModel.rotation.x = -Math.PI / 2;
        }

        // Hide fallback procedural mesh and core once high-res GLB is mounted
        if (shieldMesh) shieldMesh.visible = false;
        if (innerCore) innerCore.visible = false;

        glbWrapper.add(glbModel);

        // Studio PBR Titanium Armor Materials (Giữ nguyên texture gốc sắc nét, không ám xanh)
        glbModel.traverse((child) => {
          if (child.isMesh) {
            child.castShadow = true;
            child.receiveShadow = true;
            if (child.material) {
              child.material = child.material.clone();
              if (child.material.metalness !== undefined) {
                child.material.metalness = 0.78;
              }
              if (child.material.roughness !== undefined) {
                child.material.roughness = 0.35;
              }
              if (child.material.emissive) {
                child.material.emissive.setHex(0x000000);
                child.material.emissiveIntensity = 0.0;
              }
            }
          }
        });

        console.log('✅ Tripo3D GLB Model loaded & centered perfectly on Lock Emblem at (0,0,0)!');
      }

      // Priority 1: Instant Base64 ArrayBuffer parse (Zero CORS, 100% offline & local file:/// compatible)
      if (typeof window.CYBER_SHIELD_GLB_BASE64 === 'string' && window.CYBER_SHIELD_GLB_BASE64.length > 100) {
        try {
          const arrayBuffer = base64ToArrayBuffer(window.CYBER_SHIELD_GLB_BASE64);
          loader.parse(arrayBuffer, '', (gltf) => {
            setupGlbScene(gltf);
          }, (err) => {
            console.warn('GLTFLoader.parse base64 failed, falling back to URLs:', err);
            tryLoadModel(0);
          });
        } catch (e) {
          console.warn('Base64 ArrayBuffer decode error, fallback to URLs:', e);
          tryLoadModel(0);
        }
      } else {
        const possibleUrls = [
          '../assets/models/cyber-shield.glb',
          'assets/models/cyber-shield.glb',
          '/plagiarism/assets/models/cyber-shield.glb'
        ];

        function tryLoadModel(idx) {
          if (idx >= possibleUrls.length) return;
          loader.load(possibleUrls[idx], (gltf) => {
            setupGlbScene(gltf);
          }, undefined, (err) => {
            tryLoadModel(idx + 1);
          });
        }

        tryLoadModel(0);
      }
    }

    // Interactive Drag-to-Rotate & Hover Parallax Listeners
    container.addEventListener('mousedown', (e) => {
      isDragging = true;
      prevMouse = { x: e.clientX, y: e.clientY };
    });

    window.addEventListener('mouseup', () => {
      isDragging = false;
    });

    window.addEventListener('mousemove', (e) => {
      if (isDragging) {
        const deltaX = e.clientX - prevMouse.x;
        const deltaY = e.clientY - prevMouse.y;
        targetRotationY += deltaX * 0.009;
        targetRotationX += deltaY * 0.009;
        targetRotationX = Math.max(-0.55, Math.min(0.55, targetRotationX));
        prevMouse = { x: e.clientX, y: e.clientY };
      } else {
        const rect = container.getBoundingClientRect();
        const x = (e.clientX - rect.left) / rect.width;
        const y = (e.clientY - rect.top) / rect.height;
        if (x >= -0.3 && x <= 1.3 && y >= -0.3 && y <= 1.3) {
          targetRotationY = (x - 0.5) * 0.48;
          targetRotationX = (y - 0.5) * 0.32;
        }
      }
    });

    // Touch Support
    container.addEventListener('touchstart', (e) => {
      if (e.touches.length === 1) {
        isDragging = true;
        prevMouse = { x: e.touches[0].clientX, y: e.touches[0].clientY };
      }
    }, { passive: true });

    window.addEventListener('touchend', () => {
      isDragging = false;
    });

    window.addEventListener('touchmove', (e) => {
      if (isDragging && e.touches.length === 1) {
        const deltaX = e.touches[0].clientX - prevMouse.x;
        const deltaY = e.touches[0].clientY - prevMouse.y;
        targetRotationY += deltaX * 0.009;
        targetRotationX += deltaY * 0.009;
        targetRotationX = Math.max(-0.55, Math.min(0.55, targetRotationX));
        prevMouse = { x: e.touches[0].clientX, y: e.touches[0].clientY };
      }
    }, { passive: true });

    // Render Animation Loop
    let clock = new THREE.Clock();
    function animate() {
      animId = requestAnimationFrame(animate);
      const delta = clock.getDelta();
      const t = clock.getElapsedTime();
      const cfg = STAGE_CONFIGS[currentStage] || STAGE_CONFIGS[0];

      // Smooth inertial tilt of the entire shield group
      shieldGroup.rotation.y += (targetRotationY - shieldGroup.rotation.y) * 0.07;
      shieldGroup.rotation.x += (targetRotationX - shieldGroup.rotation.x) * 0.07;

      // Base idle gyroscopic sway pivoting around EXACT EMBLEM (0,0,0)
      if (glbWrapper) {
        glbWrapper.rotation.y = Math.sin(t * 1.1) * 0.38;
        glbWrapper.rotation.x = Math.cos(t * 0.8) * 0.09;
      }

      if (innerCore) {
        innerCore.rotation.x -= cfg.rotationSpeed * 1.5;
        innerCore.rotation.y += cfg.rotationSpeed * 2.0;
      }

      // 1. KINETIC RADAR ROTOR: 360° Continuous Sweep around Lock Emblem (0,0,0)
      if (radarRotorGroup) {
        radarRotorGroup.rotation.z -= cfg.rotationSpeed * 2.2;
      }

      // 2. DUAL COUNTER-ROTATING TECH ARCS
      if (techArcsGroup1) {
        techArcsGroup1.rotation.z += cfg.rotationSpeed * 1.4;
      }
      if (techArcsGroup2) {
        techArcsGroup2.rotation.z -= cfg.rotationSpeed * 1.1;
      }

      // 3. POLAR RETICLE DIAL
      if (reticleGroup) {
        reticleGroup.rotation.z += cfg.rotationSpeed * 0.35;
      }

      // 4. 3D GYROSCOPIC RINGS
      if (gyroRing1) gyroRing1.rotation.z += cfg.rotationSpeed * 1.0;
      if (gyroRing2) gyroRing2.rotation.x += cfg.rotationSpeed * 1.2;

      // Orbiting Quantum Photon Beacons along Gyro Rings
      if (gyroBeacons && gyroBeacons.length === 4) {
        const a1 = t * 2.0;
        gyroBeacons[0].position.set(2.05 * Math.cos(a1), 2.05 * Math.sin(a1), 0);
        gyroBeacons[1].position.set(2.05 * Math.cos(a1 + Math.PI), 2.05 * Math.sin(a1 + Math.PI), 0);

        const a2 = -t * 2.2;
        gyroBeacons[2].position.set(2.18 * Math.cos(a2), 2.18 * Math.sin(a2), 0);
        gyroBeacons[3].position.set(2.18 * Math.cos(a2 + Math.PI), 2.18 * Math.sin(a2 + Math.PI), 0);
      }

      if (particleSystem) {
        particleSystem.rotation.y += cfg.rotationSpeed * 0.7;
      }

      // 5. BIOMETRIC LASER SCANNER: Symmetrical sweep (-1.65 to +1.65)
      laserY += laserDirection * cfg.laserSpeed;
      if (laserY > 1.65) {
        laserY = 1.65;
        laserDirection = -1;
        if (curtainMesh) curtainMesh.position.y = -0.22;
      } else if (laserY < -1.65) {
        laserY = -1.65;
        laserDirection = 1;
        if (curtainMesh) curtainMesh.position.y = 0.22;
      }
      if (laserScannerGroup) {
        laserScannerGroup.position.y = laserY;
      }

      // Alarm Pulse Effect (For Stage 4 Breach) concentric around (0,0,0)
      if (cfg.alarmPulse) {
        const pulse = 1.0 + Math.sin(t * 5.5) * 0.06;
        if (glbWrapper) {
          glbWrapper.scale.set(glbBaseScale * pulse, glbBaseScale * pulse, glbBaseScale * pulse);
        }
      } else {
        if (glbWrapper) {
          glbWrapper.scale.lerp(new THREE.Vector3(glbBaseScale, glbBaseScale, glbBaseScale), 0.1);
        }
      }

      renderer.render(scene, camera);
    }
    animate();

    // Responsive Resize Handler
    const onResize = () => {
      if (!container) return;
      const w = container.clientWidth || 340;
      const h = container.clientHeight || 240;
      camera.aspect = w / h;
      camera.updateProjectionMatrix();
      renderer.setSize(w, h);
    };
    window.addEventListener('resize', onResize);
  }

  function setStage(stageIndex) {
    currentStage = stageIndex;
    const cfg = STAGE_CONFIGS[stageIndex] || STAGE_CONFIGS[0];

    // Tint GLB Model - preserve original titanium PBR texture in stage 0 (Idle)
    if (glbModel) {
      glbModel.traverse((child) => {
        if (child.isMesh && child.material) {
          if (child.material.emissive) {
            if (stageIndex === 0) {
              child.material.emissive.setHex(0x000000);
              child.material.emissiveIntensity = 0.0;
            } else if (stageIndex === 4) {
              child.material.emissive.setHex(0xef4444);
              child.material.emissiveIntensity = 0.9;
            } else {
              child.material.emissive.setHex(cfg.emissiveColor);
              child.material.emissiveIntensity = 0.35;
            }
          }
        }
      });
    }

    // Fallback mesh
    if (shieldMesh) {
      shieldMesh.material.color.setHex(cfg.shieldColor);
      shieldMesh.material.emissive.setHex(cfg.emissiveColor);
    }
    if (innerCore) innerCore.material.color.setHex(cfg.coreColor);

    // Kinetic Radar Rotor & Calipers
    if (rotorSpineMat) rotorSpineMat.color.setHex(cfg.rotorColor);
    if (rotorCrossMat) rotorCrossMat.color.setHex(cfg.coreColor);
    if (rotorHubMat) rotorHubMat.color.setHex(cfg.coreColor);
    if (rotorCaliperMat) rotorCaliperMat.color.setHex(cfg.coreColor);
    if (rotorFanMat) {
      rotorFanMat.color.setHex(cfg.rotorFanColor);
      rotorFanMat.opacity = (stageIndex === 4) ? 0.35 : 0.18;
    }

    // Counter-Rotating Tech Arcs
    if (arcsMat1) arcsMat1.color.setHex(cfg.arcsColor1);
    if (arcsMat2) arcsMat2.color.setHex(cfg.arcsColor2);
    if (arcsDotsMat) arcsDotsMat.color.setHex(cfg.coreColor);

    // Reticle Dial & Gyro Rings
    if (reticleDialMat) reticleDialMat.color.setHex(cfg.reticleColor);
    if (reticleMat) reticleMat.color.setHex(cfg.coreColor);
    if (gyroMat1) gyroMat1.color.setHex(cfg.arcsColor1);
    if (gyroMat2) gyroMat2.color.setHex(cfg.arcsColor2);

    // Quantum Beacons
    if (gyroBeacons) {
      gyroBeacons.forEach(beacon => {
        if (beacon.material) beacon.material.color.setHex(cfg.beaconColor);
      });
    }

    // Biometric Laser Scanner
    if (laserBarMat) laserBarMat.color.setHex(cfg.laserColor);
    if (laserBracketMat) laserBracketMat.color.setHex(cfg.coreColor);
    if (curtainMat) {
      curtainMat.color.setHex(cfg.laserColor);
      curtainMat.opacity = (stageIndex === 4) ? 0.38 : 0.18;
    }

    if (coreLight) coreLight.color.setHex(cfg.coreColor);
    if (particleSystem) particleSystem.material.color.setHex(cfg.shieldColor);

    // Sync Quantum Containment Pillars with 3D Shield Stage
    const leftPillar = document.getElementById('containment-pillar-left');
    const rightPillar = document.getElementById('containment-pillar-right');
    if (leftPillar && rightPillar) {
      if (stageIndex === 4) {
        leftPillar.classList.add('stage-breach');
        rightPillar.classList.add('stage-breach');
      } else {
        leftPillar.classList.remove('stage-breach');
        rightPillar.classList.remove('stage-breach');
      }
    }
  }

  function reset() {
    setStage(0);
  }

  global.CyberShield3D = {
    init: initCyberShield3D,
    setStage: setStage,
    reset: reset,
    getStage: () => currentStage
  };

  document.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
      initCyberShield3D('batch-shield-3d-viewport');
    }, 150);
  });
})(window);