# 🚜 GL Vehicles - Hoja de Roadmap y Errores (Actualizado)

## 🛠️ Tareas Pendientes (Backlog)

### 🧩 Mecánicas y Componentes
- [ ] **Restricciones de Slots (Ampliación):** Prohibir meter objetos no válidos en los slots de Accesorios (solo deben aceptar Attachments).
- [ ] **Visualización de Estadísticas:** Ver (Grip, Aceleración, Peso, etc.) en la GUI o al mirar el vehículo.
- [ ] **Sistema de Pintura:** Click derecho con tintes para cambiar el color del chasis.
- [ ] **Alertas de Combustible:** Añadir mensaje en actionbar mientras se llena el depósito en la Gas Pump.
- [ ] **Luces:** Huesos funcionales para faros que iluminen de noche (Emissive Textures).
- [ ] **Sonidos Custom:** Reemplazar los *placeholders* musicales por sonidos reales de motor `.ogg`.

### 🏗️ Progresión y Mundo
- [ ] **Mesa de Trabajo Especial:** 
    - [x] Registro de bloque y ScreenHandler (GUI base lista).
    - [ ] **Recetas de Fabricación:** Implementar la lógica de crafteo real (materiales -> ítems).
    - [ ] **Visuales:** Modelo y textura de la mesa.
- [ ] **Crafteos:** Definir recetas balanceadas para todos los componentes.

### 🚜 Accesorios (Attachments)
- [ ] **Harvester, Trailer, Seeder:**
    - [x] Registro de ítems (Iconos generados).
    - [x] **Lógica Funcional Base:** Implementada cosecha automática (Harvester) y siembra (Seeder) en `TractorEntity`.
    - [ ] **Remolque (Trailer):** Implementar la lógica de enganche visual (ahora es solo inventario).
    - [ ] **Visuales GeckoLib:** Huesos dinámicos en el modelo del tractor para los accesorios enganchados.

### 🏗️ Automatización y Silos
- [x] **Rejilla de Descarga:** Bloque de suelo funcional para vaciado automático.
- [x] **Tubo de Sinfín (Auger):** Traslado de ítems direccional implementado.
- [ ] **Tanques de Almacenamiento:** Bloques de metal corrugado (Silo Wall registrado, falta lógica de capacidad).
- [ ] **Animación visual:** Añadir texturas y modelos específicos para los tubos y rejillas.

---

## ✅ Completado (Hitos Alcanzados)

### 🏎️ Motor de Físicas Premium (Antigravity v2.0)
- [x] **Hibridación de Red:** Predicción en el cliente con reconciliación en el servidor para 0 lag.
- [x] **Caja de Cambios Secuencial:** 5 marchas reales con retardos de embrague y clipping de RPM.
- [x] **Sistema de Marcha Atrás (R):** Inteligente, requiere estar casi parado para evitar errores.
- [x] **Peso e Inercia:** La estadística `Weight` ahora divide la aceleración y limita la velocidad punta.
- [x] **Coast & Friction:** Los vehículos mantienen la inercia al bajarse y se detienen suavemente.
- [x] **Subviraje Realista:** El ángulo de giro se endurece dinámicamente según la velocidad.
- [x] **Stats Sincronizados:** Poder, aceleración y velocidad sincronizados vía DataTracker para una predicción perfecta.

### 📊 Interfaz y HUD
- [x] **Dashboard Dinámico:** Gear actual (P/1-5/R), velocímetro (km/h) y barra de RPM con gradiente.
- [x] **Limitador Visual:** El HUD parpadea en rojo al llegar al corte de inyección.
- [x] **Barra de Combustible:** HUD vertical persistente.
- [x] **Indicador de Carga:** El HUD muestra la ocupación del inventario del accesorio enganchado.

### 🧩 Componentes y Mantenimiento
- [x] **Piezas Especiales:** Motor 1.9 TDI con estadísticas únicas (Ultra-durabilidad).
- [x] **Sistema de Desgaste (Wear):** Las piezas se degradan con el uso y afectan al rendimiento.
- [x] **Reparaciones:** Llave inglesa (Wrench) para abrir el inventario o desguazar vehículos destruidos.
- [x] **Daño por Colisión:** Daño modular al chasis y al conductor según la fuerza del impacto.

### 🎨 Visual y Renderizado
- [x] **Posición del Conductor:** Altura corregida para Tractor (0.53) y Kart (0.15).
- [x] **Animaciones:** Idle y Move balanceados según la velocidad de la entidad.
- [x] **Sincronización de Rotación:** Yaw manual interpolado para giros perfectos en multiplayer.
- [x] **Integración GeckoLib 4.**
