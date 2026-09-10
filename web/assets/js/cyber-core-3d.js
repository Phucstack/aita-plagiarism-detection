/**
 * AITA 2026 3D AST Syntax Tree Visualizer (Three.js WebGL Engine)
 * Replaces decorative sphere with a meaningful, interactive JavaParser 3.25 Abstract Syntax Tree
 * Visualizes code structure, subtree duplication (88.5% Flag), and token aliasing (_cart -> _basket)
 * Designed for PRJ301 - AITA Group 4 (AI Plagiarism & Code Similarity)
 */
(function (global) {
  function initCyberCore3D(containerId = 'cyber-3d-viewport') {
    const container = document.getElementById(containerId);
    if (!container || typeof THREE === 'undefined') return;

    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      return;
    }

    const width = container.clientWidth || 380;
    const height = container.clientHeight || 190;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(50, width / height, 0.1, 1000);
    camera.position.set(0, 0, 39);

    const renderer = new THREE.WebGLRenderer({
      alpha: true,
      antialias: true,
      powerPreference: "high-performance"
    });
    renderer.setSize(width, height);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

    // Clear existing canvas
    while (container.firstChild) {
      container.removeChild(container.firstChild);
    }
    container.appendChild(renderer.domElement);

    // Overlaid HUD Tooltip inside container (Hover-Activated to never obscure the root node)
    let tooltipEl = document.getElementById('ast-3d-tooltip');
    if (!tooltipEl) {
      tooltipEl = document.createElement('div');
      tooltipEl.id = 'ast-3d-tooltip';
      tooltipEl.className = 'absolute top-1.5 left-2 right-2 pointer-events-none px-2.5 py-1 rounded-lg bg-[#070914]/95 border border-cyan-500/50 text-[10px] font-mono flex items-center justify-between shadow-2xl backdrop-blur-md transition-all duration-200 z-20 opacity-0 pointer-events-none';
      tooltipEl.innerHTML = ``;
      container.style.position = 'relative';
      container.appendChild(tooltipEl);
    }

    // Root Group for 3D Orbiting (Shifted down to perfectly center in viewport)
    const treeGroup = new THREE.Group();
    treeGroup.position.set(0, -3.2, 0);
    scene.add(treeGroup);

    // 1. Concrete AST Node Hierarchy (JavaParser 3.25 Model of OrderManager.java)
    const astNodesData = [
      // Level 0: CompilationUnit (Root)
      { id: 0, name: 'CompilationUnit', label: 'OrderManager.java', pos: [0, 11, 0], type: 'ROOT', color: 0x06b6d4, size: 2.2, desc: 'Gốc tệp Java - Chứa toàn bộ package & import', status: 'ROOT' },

      // Level 1: ClassDeclaration
      { id: 1, name: 'ClassDeclaration', label: 'public class OrderManager', pos: [0, 5.5, 0], type: 'CLASS', color: 0x38bdf8, size: 1.8, desc: 'Định nghĩa lớp thực thể OrderManager', status: 'CLASS', parentId: 0 },

      // Level 2: MethodDeclarations
      { id: 2, name: 'MethodDeclaration', label: 'calculateTotal()', pos: [-12, -0.5, 2], type: 'METHOD', color: 0xf43f5e, size: 1.7, desc: 'CỜ ĐỎ AST 88.5%: Trùng lặp cấu trúc khối lệnh tính tiền', status: 'FLAGGED', parentId: 1 },
      { id: 3, name: 'MethodDeclaration', label: 'processPayment()', pos: [0, -1.5, -3], type: 'METHOD', color: 0xf43f5e, size: 1.7, desc: 'CỜ ĐỎ AST 84.0%: Trùng lặp luồng xử lý trừ kho & thanh toán', status: 'FLAGGED', parentId: 1 },
      { id: 4, name: 'MethodDeclaration', label: 'updateStock()', pos: [12, -0.5, 1], type: 'METHOD', color: 0x10b981, size: 1.7, desc: 'AN TOÀN: Logic độc lập, độ tương đồng thấp (12%)', status: 'CLEAN', parentId: 1 },

      // Level 3: Subtree Statements & Variable Tokens (Under calculateTotal)
      { id: 5, name: 'TokenAliasing', label: '_cart ➔ _basket', pos: [-17, -7.5, 3], type: 'TOKEN', color: 0xf59e0b, size: 1.2, desc: 'Đổi tên biến cục bộ: JavaParser chuẩn hóa thành var_0', status: 'RENAME', parentId: 2 },
      { id: 6, name: 'AssignExpr', label: 'finalCost = P*finalCost', pos: [-12, -8.5, 0], type: 'STMT', color: 0xf43f5e, size: 1.2, desc: 'Biểu thức gán có cấu trúc AST tương đồng 96%', status: 'FLAGGED', parentId: 2 },
      { id: 7, name: 'ReturnStmt', label: 'return cart;', pos: [-7, -7.5, 4], type: 'STMT', color: 0x06b6d4, size: 1.1, desc: 'Câu lệnh trả về kết quả giỏ hàng', status: 'CLEAN', parentId: 2 },

      // Level 3: Subtree Statements (Under processPayment)
      { id: 8, name: 'Parameter', label: 'List<OrderItem> items', pos: [-4, -8.5, -4], type: 'TOKEN', color: 0x06b6d4, size: 1.1, desc: 'Tham số đầu vào phương thức thanh toán', status: 'CLEAN', parentId: 3 },
      { id: 9, name: 'IfStatement', label: 'if (stock < amount)', pos: [0, -9.5, -2], type: 'STMT', color: 0xf43f5e, size: 1.2, desc: 'Khối điều kiện kiểm tra tồn kho trùng lặp 88%', status: 'FLAGGED', parentId: 3 },
      { id: 10, name: 'TokenAliasing', label: 'total_amt ➔ final_cost', pos: [4, -8.5, -5], type: 'TOKEN', color: 0xf59e0b, size: 1.2, desc: 'Đổi tên biến tính tổng tiền', status: 'RENAME', parentId: 3 },

      // Level 3: Subtree Statements (Under updateStock - Clean Subtree)
      { id: 11, name: 'ForStatement', label: 'for(Item it : items)', pos: [8, -8, 2], type: 'STMT', color: 0x10b981, size: 1.1, desc: 'Vòng lặp duyệt danh sách mặt hàng độc lập', status: 'CLEAN', parentId: 4 },
      { id: 12, name: 'MethodCall', label: 'db.updateQuantity()', pos: [14, -8.5, -1], type: 'STMT', color: 0x10b981, size: 1.1, desc: 'Gọi tầng DAO cập nhật CSDL chuẩn', status: 'CLEAN', parentId: 4 }
    ];

    const nodeMeshes = [];
    const nodeLookup = {};

    // Create 3D Meshes for Nodes
    astNodesData.forEach(data => {
      const geo = new THREE.SphereGeometry(data.size, 16, 16);
      const mat = new THREE.MeshBasicMaterial({
        color: data.color,
        wireframe: false
      });
      const mesh = new THREE.Mesh(geo, mat);
      mesh.position.set(data.pos[0], data.pos[1], data.pos[2]);
      mesh.userData = data;

      // Add a glowing wireframe ring / outer shell
      const outerGeo = new THREE.IcosahedronGeometry(data.size * 1.35, 1);
      const outerMat = new THREE.MeshBasicMaterial({
        color: data.color,
        wireframe: true,
        transparent: true,
        opacity: data.status === 'FLAGGED' ? 0.75 : 0.35
      });
      const outerMesh = new THREE.Mesh(outerGeo, outerMat);
      mesh.add(outerMesh);
      mesh.userData.outerMesh = outerMat;

      treeGroup.add(mesh);
      nodeMeshes.push(mesh);
      nodeLookup[data.id] = mesh;
    });

    // 2. Create Tree Edges (Syntax Branch Connections)
    const edgePairs = [
      [0, 1], // Root -> Class
      [1, 2], // Class -> calculateTotal
      [1, 3], // Class -> processPayment
      [1, 4], // Class -> updateStock
      [2, 5], [2, 6], [2, 7], // calculateTotal children
      [3, 8], [3, 9], [3, 10], // processPayment children
      [4, 11], [4, 12] // updateStock children
    ];

    const edges = [];
    edgePairs.forEach(([parentId, childId]) => {
      const pNode = nodeLookup[parentId];
      const cNode = nodeLookup[childId];
      if (!pNode || !cNode) return;

      const points = [pNode.position.clone(), cNode.position.clone()];
      const edgeGeo = new THREE.BufferGeometry().setFromPoints(points);

      // Color depends on child status
      let lineColor = 0x06b6d4;
      if (cNode.userData.status === 'FLAGGED') lineColor = 0xf43f5e;
      else if (cNode.userData.status === 'RENAME') lineColor = 0xf59e0b;
      else if (cNode.userData.status === 'CLEAN') lineColor = 0x10b981;

      const edgeMat = new THREE.LineBasicMaterial({
        color: lineColor,
        transparent: true,
        opacity: cNode.userData.status === 'FLAGGED' ? 0.65 : 0.4,
        linewidth: 2
      });

      const line = new THREE.Line(edgeGeo, edgeMat);
      treeGroup.add(line);
      edges.push({ line, p1: pNode.position, p2: cNode.position, color: lineColor });
    });

    // 3. Traversal Photons (Animated Depth-First Search Synapse Pulses)
    const photonCount = 14;
    const photonGeo = new THREE.SphereGeometry(0.4, 8, 8);
    const photonMat = new THREE.MeshBasicMaterial({ color: 0xffffff });
    const photons = [];

    for (let i = 0; i < photonCount; i++) {
      const pMesh = new THREE.Mesh(photonGeo, photonMat);
      const edgeIdx = i % edges.length;
      pMesh.userData = {
        edgeIdx: edgeIdx,
        progress: Math.random(),
        speed: 0.008 + Math.random() * 0.006
      };
      treeGroup.add(pMesh);
      photons.push(pMesh);
    }

    // 4. Raycaster for Mouse Hover & Orbit Dragging
    const raycaster = new THREE.Raycaster();
    const mouse = new THREE.Vector2(-999, -999);
    let hoveredMesh = null;
    let lastSoundTime = 0;

    let isDragging = false;
    let prevMousePos = { x: 0, y: 0 };
    let targetRotY = 0;
    let targetRotX = 0;

    function onPointerMove(e) {
      const rect = container.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      mouse.x = (x / rect.width) * 2 - 1;
      mouse.y = -(y / rect.height) * 2 + 1;

      if (isDragging) {
        const deltaX = e.clientX - prevMousePos.x;
        const deltaY = e.clientY - prevMousePos.y;
        targetRotY += deltaX * 0.008;
        targetRotX += deltaY * 0.008;
        prevMousePos = { x: e.clientX, y: e.clientY };
      }
    }

    function onPointerDown(e) {
      isDragging = true;
      prevMousePos = { x: e.clientX, y: e.clientY };
    }

    function onPointerUp() {
      isDragging = false;
    }

    container.addEventListener('pointermove', onPointerMove);
    container.addEventListener('pointerdown', onPointerDown);
    window.addEventListener('pointerup', onPointerUp);

    // 5. Animation Loop
    let animationFrameId;
    let clock = new THREE.Clock();

    function animate() {
      animationFrameId = requestAnimationFrame(animate);
      const time = clock.getElapsedTime();

      // Smooth Orbit Drag Rotation
      treeGroup.rotation.y += (targetRotY - treeGroup.rotation.y) * 0.08;
      treeGroup.rotation.x += (targetRotX - treeGroup.rotation.x) * 0.08;

      // Gentle Resting Sway when not dragging
      if (!isDragging) {
        treeGroup.rotation.y += 0.0025;
        treeGroup.rotation.z = Math.sin(time * 0.8) * 0.03;
      }

      // Warning Pulse on FLAGGED Nodes
      nodeMeshes.forEach(mesh => {
        if (mesh.userData.status === 'FLAGGED') {
          const pulse = 1.0 + Math.sin(time * 4) * 0.12;
          mesh.scale.set(pulse, pulse, pulse);
        } else if (mesh !== hoveredMesh) {
          mesh.scale.set(1, 1, 1);
        }
      });

      // Move Traversal Photons along Edges
      photons.forEach(p => {
        p.userData.progress += p.userData.speed;
        if (p.userData.progress > 1) {
          p.userData.progress = 0;
          p.userData.edgeIdx = Math.floor(Math.random() * edges.length);
        }
        const edge = edges[p.userData.edgeIdx];
        if (edge) {
          p.position.lerpVectors(edge.p1, edge.p2, p.userData.progress);
        }
      });

      // Raycast Hover Interaction
      raycaster.setFromCamera(mouse, camera);
      const intersects = raycaster.intersectObjects(nodeMeshes);

      if (intersects.length > 0) {
        const hit = intersects[0].object;
        if (hoveredMesh !== hit) {
          hoveredMesh = hit;
          hoveredMesh.scale.set(1.4, 1.4, 1.4);

          const data = hit.userData;
          let badgeColor = 'text-cyan-400 border-cyan-500/40 bg-cyan-950/70';
          let statusText = '🟢 GỐC / HỢP LỆ';
          if (data.status === 'FLAGGED') {
            badgeColor = 'text-rose-400 border-rose-500/50 bg-rose-950/80 animate-pulse';
            statusText = '🔴 CỜ ĐỎ AST (88.5%)';
          } else if (data.status === 'RENAME') {
            badgeColor = 'text-amber-400 border-amber-500/50 bg-amber-950/80';
            statusText = '🟡 ĐỔI TÊN BIẾN';
          } else if (data.status === 'CLEAN') {
            badgeColor = 'text-emerald-400 border-emerald-500/40 bg-emerald-950/70';
            statusText = '🟢 AN TOÀN';
          }

          if (tooltipEl) {
            tooltipEl.innerHTML = `
              <div class="flex items-center gap-2 truncate">
                <span class="px-1.5 py-0.5 rounded text-[9px] font-bold border ${badgeColor}">
                  ${statusText}
                </span>
                <span class="text-white font-bold truncate">${data.name}: <span class="text-cyan-300 font-mono">${data.label}</span></span>
              </div>
              <span class="text-slate-300 text-[9px] hidden sm:block truncate ml-2">${data.desc}</span>
            `;
            tooltipEl.classList.remove('opacity-0');
            tooltipEl.classList.add('opacity-100');
          }

          const now = performance.now();
          if (now - lastSoundTime > 120 && window.CyberAudio && CyberAudio.playTick) {
            CyberAudio.playTick();
            lastSoundTime = now;
          }
        }
      } else if (hoveredMesh) {
        hoveredMesh = null;
        if (tooltipEl) {
          tooltipEl.classList.remove('opacity-100');
          tooltipEl.classList.add('opacity-0');
        }
      }

      renderer.render(scene, camera);
    }
    animate();

    // 6. Handle Resize
    function onResize() {
      if (!container) return;
      const nw = container.clientWidth || 380;
      const nh = container.clientHeight || 190;
      camera.aspect = nw / nh;
      camera.updateProjectionMatrix();
      renderer.setSize(nw, nh);
    }
    window.addEventListener('resize', onResize);

    return {
      destroy: function () {
        cancelAnimationFrame(animationFrameId);
        container.removeEventListener('pointermove', onPointerMove);
        container.removeEventListener('pointerdown', onPointerDown);
        window.removeEventListener('pointerup', onPointerUp);
        window.removeEventListener('resize', onResize);
      }
    };
  }

  global.initCyberCore3D = initCyberCore3D;

  function autoInit() {
    if (document.getElementById('cyber-3d-viewport')) {
      global.cyberCore3DInstance = initCyberCore3D('cyber-3d-viewport');
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', autoInit);
  } else {
    autoInit();
  }
})(typeof window !== 'undefined' ? window : this);
