package com.gl.vehicles.entity;

import com.gl.vehicles.GLVehicles;
import com.gl.vehicles.gui.VehicleScreenHandler;
import com.gl.vehicles.item.EngineItem;
import com.gl.vehicles.item.ModItems;
import com.gl.vehicles.item.WheelItem;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class AbstractVehicleEntity extends Entity implements ExtendedScreenHandlerFactory, GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected static final TrackedData<Integer> COLOR = DataTracker.registerData(AbstractVehicleEntity.class,
            TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Float> AERODYNAMICS = DataTracker.registerData(AbstractVehicleEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    protected static final TrackedData<Float> FUEL = DataTracker.registerData(AbstractVehicleEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> SYNC_YAW = DataTracker.registerData(AbstractVehicleEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Integer> GEAR = DataTracker.registerData(AbstractVehicleEntity.class,
            TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Float> RPM_SYNC = DataTracker.registerData(AbstractVehicleEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> CHASSIS_HEALTH = DataTracker.registerData(AbstractVehicleEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Boolean> IS_DESTROYED = DataTracker.registerData(AbstractVehicleEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Integer> OCCUPIED_SLOTS = DataTracker.registerData(AbstractVehicleEntity.class,
            TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Float> ACCEL_SYNC = DataTracker.registerData(AbstractVehicleEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> MAX_SPEED_SYNC = DataTracker.registerData(AbstractVehicleEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> FORWARD_SPEED_SYNC = DataTracker.registerData(AbstractVehicleEntity.class,
            TrackedDataHandlerRegistry.FLOAT);

    public static final float MAX_CHASSIS_HEALTH = 300.0f;

    protected final SimpleInventory inventory;
    protected float steering = 0.0f;
    protected boolean inputForward, inputBackward, inputLeft, inputRight, inputJump;

    protected double forwardSpeed = 0.0;
    protected float vehicleYaw = 0.0f;
    protected float accelerationStat = 0.0f;
    protected float maxSpeed = 0.0f;
    protected float grip = 1.0f;
    protected float weight = 100.0f;

    // --- DRIFT ESTILO MARIO KART ---
    protected boolean isDrifting = false; // ¿Estamos en drift activo?
    protected int driftDir = 0; // Dirección bloqueada: -1 = izq, +1 = der
    protected float driftAngle = 0.0f; // Ángulo de apertura acumulado (0 = cerrado)
    // Velocidad angular propia del drift, independiente del steering normal
    protected float driftYawRate = 0.0f;

    // --- SISTEMA DE MARCHAS ---
    protected int currentGear = 1;
    protected int shiftTimer = 0;
    protected float rpm = 0.0f;
    protected boolean isLimiting = false;

    // --- SISTEMA DE DAÑO ---
    protected Vec3d prevVelocityForCollision = Vec3d.ZERO;
    protected int smokeTimer = 0;
    protected int wrenchTimer = 0;
    protected java.util.UUID lastWrenchPlayer = null;

    public AbstractVehicleEntity(EntityType<? extends AbstractVehicleEntity> type, World world, int inventorySize) {
        super(type, world);
        this.inventory = new SimpleInventory(inventorySize);
        this.inventory.addListener(sender -> this.calculateStats());
        this.vehicleYaw = this.getYaw();
        this.setStepHeight(1.0f); // Permite subir bloques/slabs sin trompicones
        this.calculateStats();
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(COLOR, 0xFFFFFF);
        this.dataTracker.startTracking(AERODYNAMICS, 1.0f);
        this.dataTracker.startTracking(FUEL, 100.0f);
        this.dataTracker.startTracking(SYNC_YAW, 0.0f);
        this.dataTracker.startTracking(GEAR, 1);
        this.dataTracker.startTracking(RPM_SYNC, 0.0f);
        this.dataTracker.startTracking(CHASSIS_HEALTH, MAX_CHASSIS_HEALTH);
        this.dataTracker.startTracking(IS_DESTROYED, false);
        this.dataTracker.startTracking(OCCUPIED_SLOTS, 0);
        this.dataTracker.startTracking(ACCEL_SYNC, 0.0f);
        this.dataTracker.startTracking(MAX_SPEED_SYNC, 0.0f);
        this.dataTracker.startTracking(FORWARD_SPEED_SYNC, 0.0f);
    }

    // BAJAR AL JUGADOR (Estaba muy alto)
    @Override
    public double getMountedHeightOffset() {
        return 0.34D;
    }

    @Override
    public float getYaw(float tickDelta) {
        return this.dataTracker.get(SYNC_YAW);
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z,
            float yaw, float pitch, int stepsCount, boolean interpolate) {
        if (isLocalPlayerDriving()) {
            return;
        }
        super.updateTrackedPositionAndAngles(x, y, z, yaw, pitch, 10, true);
    }

    @Override
    public float getHeadYaw() {
        return this.dataTracker.get(SYNC_YAW);
    }

    @Override
    public float getBodyYaw() {
        return this.dataTracker.get(SYNC_YAW);
    }

    public float getRpm() {
        return this.rpm;
    }

    public int getGear() {
        return this.currentGear;
    }

    public double getForwardSpeed() {
        return (float) this.forwardSpeed;
    }

    public float getGrip() {
        return this.grip;
    }

    public float getFuel() {
        return this.dataTracker.get(FUEL);
    }

    public void setFuel(float fuel) {
        this.dataTracker.set(FUEL, Math.max(0, Math.min(100.0f, fuel)));
    }

    public void calculateStats() {
        int occupied = 0;
        if (this.inventory.size() > 4) {
            for (int i = 4; i < this.inventory.size(); i++) {
                if (!this.inventory.getStack(i).isEmpty())
                    occupied++;
            }
        }
        this.dataTracker.set(OCCUPIED_SLOTS, occupied);

        float enginePower = 0.0f;
        float tireGrip = 0.0f;
        float tireWear = 1.0f;
        boolean hasEngine = !getEngineStack().isEmpty();
        boolean hasWheels = !getWheelStack().isEmpty();

        if (hasEngine && getEngineStack().getItem() instanceof EngineItem engine) {
            enginePower = engine.getSpeedMultiplier();
        }

        if (hasWheels && getWheelStack().getItem() instanceof WheelItem wheel) {
            tireGrip = wheel.getGrip();
            tireWear = Math.max(0.1f, 1.0f - getWear(getWheelStack()));
        }

        this.weight = getBaseWeight() + getAttachmentWeight() + 30.0f;

        // El peso de referencia es 100. A más peso, menos aceleración y punta.
        float weightFactor = 100.0f / this.weight;

        if (hasEngine && hasWheels) {
            this.grip = Math.max(0.1f, (tireGrip / tireWear));

            // La potencia base se ve afectada directamente por el factor de peso
            this.accelerationStat = (0.024f * enginePower) * weightFactor;
            this.maxSpeed = (0.90f * enginePower) * (float) Math.sqrt(weightFactor);
        } else {
            this.grip = 0.0f;
            this.accelerationStat = 0.0f;
            this.maxSpeed = 0.0f;
            this.forwardSpeed = 0.0;
        }

        // Sincronizar con el cliente si estamos en el servidor
        if (!this.getWorld().isClient) {
            this.dataTracker.set(ACCEL_SYNC, this.accelerationStat);
            this.dataTracker.set(MAX_SPEED_SYNC, this.maxSpeed);
            this.dataTracker.set(GEAR, this.currentGear);
        }
    }

    protected float getAttachmentWeight() {
        float w = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (i == 1 || i == 2)
                continue;
            if (!inventory.getStack(i).isEmpty())
                w += 30.0f;
        }
        return w;
    }

    private float getWear(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains("Wear"))
            return stack.getNbt().getFloat("Wear");
        return 0.0f;
    }

    private void applyWear(ItemStack stack, float amount) {
        if (stack.isEmpty())
            return;
        NbtCompound nbt = stack.getOrCreateNbt();
        float wear = nbt.getFloat("Wear") + amount;
        if (wear >= 1.0f) {
            stack.setCount(0);
            calculateStats();
        } else
            nbt.putFloat("Wear", wear);
    }

    @Override
    public void tick() {
        if (!this.getWorld().isClient && wrenchTimer > 0)
            wrenchTimer--;
        if (this.getWorld().isClient) {
            this.vehicleYaw = this.dataTracker.get(SYNC_YAW);
            this.setYaw(this.vehicleYaw);
            this.prevYaw = this.vehicleYaw;
            this.setHeadYaw(this.vehicleYaw);
            this.setBodyYaw(this.vehicleYaw);

            boolean destroyed = this.dataTracker.get(IS_DESTROYED);
            if (this.hasPassengers() && !destroyed && !getEngineStack().isEmpty()) {
                float rpmVal = this.dataTracker.get(RPM_SYNC);
                float basePitch = 0.6f;
                float maxPitch = 1.4f;

                ItemStack engineStack = getEngineStack();
                if (engineStack.getItem() instanceof com.gl.vehicles.item.EngineItem engine) {
                    basePitch = engine.getBasePitch();
                    maxPitch = engine.getMaxPitch();
                }

                float pitch = basePitch + rpmVal * (maxPitch - basePitch);
                this.getWorld().playSound(
                        this.getX(), this.getY(), this.getZ(),
                        net.minecraft.sound.SoundEvents.BLOCK_FIRE_EXTINGUISH,
                        net.minecraft.sound.SoundCategory.NEUTRAL,
                        1.0f, pitch, false);
            }

            if (this.age % 3 == 0) {
                float fuel = this.dataTracker.get(FUEL);
                float health = this.dataTracker.get(CHASSIS_HEALTH);
                float healthPct = health / MAX_CHASSIS_HEALTH;
                if (fuel < 5.0f || healthPct < 0.2f || destroyed) {
                    net.minecraft.util.math.random.Random rand = this.getWorld().getRandom();
                    double ox = (rand.nextDouble() - 0.5) * 0.4;
                    double oz = (rand.nextDouble() - 0.5) * 0.4;
                    this.getWorld().addParticle(
                            net.minecraft.particle.ParticleTypes.LARGE_SMOKE,
                            this.getX() + ox, this.getY() + this.getHeight() * 0.8, this.getZ() + oz,
                            0, 0.04, 0);
                }
            }

            // --- PARTÍCULAS DE DERRAPE (CLIENTE) ---
            if (isDrifting && Math.abs(forwardSpeed) > 0.15) {
                net.minecraft.util.math.random.Random rand = this.getWorld().getRandom();
                // Calcular posición trasera del vehículo (aproximada)
                double angle = Math.toRadians(this.getYaw());
                double ox = Math.sin(angle) * 1.2;
                double oz = -Math.cos(angle) * 1.2;

                // También partículas laterales según la dirección del drift
                double sideAngle = angle + Math.PI / 2.0 * driftDir;
                double sx = Math.sin(sideAngle) * 0.6;
                double sz = -Math.cos(sideAngle) * 0.6;

                for (int i = 0; i < 3; i++) {
                    this.getWorld().addParticle(
                            net.minecraft.particle.ParticleTypes.SMOKE,
                            this.getX() + ox + sx + (rand.nextDouble() - 0.5) * 0.4,
                            this.getY(),
                            this.getZ() + oz + sz + (rand.nextDouble() - 0.5) * 0.4,
                            0, 0.01, 0);
                }
            }
        }
        super.tick();

        boolean destroyed = this.dataTracker.get(IS_DESTROYED);
        float syncedAccel = this.dataTracker.get(ACCEL_SYNC);
        float syncedMax = this.dataTracker.get(MAX_SPEED_SYNC);
        float fuel = getFuel();

        // En el cliente usamos los datos sincronizados para canMove
        // En el servidor podríamos usar los reales, pero los sincronizados ya son
        // copia.
        boolean canMove = !destroyed && syncedAccel > 0 && syncedMax > 0 && fuel > 0;

        // Actualizar variables locales en cliente para que handlePhysics() funcione
        if (this.getWorld().isClient) {
            this.accelerationStat = syncedAccel;
            this.maxSpeed = syncedMax;
            if (!isLocalPlayerDriving()) {
                // Suavizado del cambio de velocidad sincronizada para quitar el micro-stutter
                this.forwardSpeed = MathHelper.lerp(0.3f, (float) this.forwardSpeed,
                        this.dataTracker.get(FORWARD_SPEED_SYNC));
            }
        }

        // --- MOVIMIENTO Y FÍSICAS ---
        if (this.getWorld().isClient) {
            // Predicción para el conductor local
            if (isLocalPlayerDriving() && canMove) {
                handlePhysics();
                this.move(MovementType.SELF, this.getVelocity());
            }
        } else {
            // Lógica del Servidor
            if (this.hasPassengers() && canMove) {
                handlePhysics();
                if (Math.abs(forwardSpeed) > 0.01) {
                    float fuelUsage = 0.005f;
                    float wearAmount = 0.00001f;
                    ItemStack engineStack = getEngineStack();
                    if (engineStack.getItem() instanceof com.gl.vehicles.item.EngineItem engine) {
                        fuelUsage *= engine.getFuelConsumption();
                        wearAmount *= engine.getWearMultiplier();
                    }
                    setFuel(getFuel() - fuelUsage);
                    applyWear(getEngineStack(), wearAmount);
                    applyWear(getWheelStack(), 0.00002f);
                }
            } else {
                // Si no hay pasajeros, aplicar rozamiento natural (coast) y parking
                this.currentGear = 0;
                this.dataTracker.set(GEAR, 0);

                if (Math.abs(forwardSpeed) > 0.01) {
                    // El rozamiento ahora depende del AGARRE (Grip)
                    // A más grip, el coche frena antes por el rozamiento con el suelo.
                    float gripFriction = MathHelper.clamp(0.92f - (this.grip * 0.05f), 0.70f, 0.96f);
                    applyFriction(gripFriction);
                } else {
                    forwardSpeed = 0;
                    // FRENAR EMPUJONES EXTERNOS: Reducimos la inercia drásticamente cada tick
                    Vec3d v = this.getVelocity();
                    this.setVelocity(v.x * 0.5, v.y, v.z * 0.5);
                }
            }
            this.dataTracker.set(FORWARD_SPEED_SYNC, (float) this.forwardSpeed);
            this.move(MovementType.SELF, this.getVelocity());

            if (!this.isOnGround()) {
                this.setVelocity(this.getVelocity().add(0, -0.04, 0));
            }
        }

        if (this.getWorld().isClient)
            return;

        if ((inputForward || inputBackward || inputLeft || inputRight) && this.age % 20 == 0) {
            sendDriverAlerts();
        }
    }

    public void applyChassisHit(float damage) {
        float health = this.dataTracker.get(CHASSIS_HEALTH);
        health = Math.max(0, health - damage);
        this.dataTracker.set(CHASSIS_HEALTH, health);
        if (health <= 0 && !this.dataTracker.get(IS_DESTROYED)) {
            this.dataTracker.set(IS_DESTROYED, true);
            this.removeAllPassengers();
            if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                sw.spawnParticles(net.minecraft.particle.ParticleTypes.LARGE_SMOKE,
                        this.getX(), this.getY() + 0.5, this.getZ(), 20, 0.5, 0.5, 0.5, 0.02);
            }
        }
    }

    public float getChassisHealth() {
        return this.dataTracker.get(CHASSIS_HEALTH);
    }

    public boolean isDestroyed() {
        return this.dataTracker.get(IS_DESTROYED);
    }

    private boolean isLocalPlayerDriving() {
        if (!this.getWorld().isClient)
            return false;
        Entity driver = this.getFirstPassenger();
        return driver instanceof PlayerEntity
                && driver.getUuid().equals(net.minecraft.client.MinecraftClient.getInstance().player.getUuid());
    }

    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        if (!this.getWorld().isClient && source.getAttacker() instanceof PlayerEntity player) {
            ItemStack held = player.getMainHandStack();
            if (held.isOf(ModItems.WRENCH)) {
                if (wrenchTimer > 0 && lastWrenchPlayer != null && lastWrenchPlayer.equals(player.getUuid())) {
                    repairOrBreak();
                    return true;
                } else {
                    wrenchTimer = 40;
                    lastWrenchPlayer = player.getUuid();
                    player.sendMessage(
                            Text.literal("⚠️ Click IZQUIERDO de nuevo para desguazar").formatted(Formatting.YELLOW),
                            true);
                    return true;
                }
            }
        }
        return super.damage(source, amount);
    }

    private void sendDriverAlerts() {
        if (!(this.getFirstPassenger() instanceof ServerPlayerEntity driver))
            return;
        boolean hasEngine = !getEngineStack().isEmpty();
        boolean hasWheels = !getWheelStack().isEmpty();
        if (getFuel() <= 0) {
            driver.sendMessage(Text.literal("⛽ SIN COMBUSTIBLE").formatted(Formatting.RED), true);
        } else if (!hasEngine || !hasWheels) {
            driver.sendMessage(Text.literal("❌ Faltan componentes (Motor/Ruedas)").formatted(Formatting.RED), true);
        }
    }

    protected void handlePhysics() {
        // Asegurar que maxSpeed no sea 0 para evitar divisiones inválidas
        double safeMaxSpeed = Math.max(0.1, maxSpeed);
        double absSpeed = Math.abs(forwardSpeed);
        double speedPercent = absSpeed / safeMaxSpeed;

        // --- RANGOS DE CAMBIO (Estirados con Clipping) ---
        float m1Max = 0.28f;
        float m2Max = 0.52f;
        float m3Max = 0.65f; // Acortada la 3ª para que la 4ª sea más larga
        float m4Max = 0.78f; // Quinta marcha desde el 78% (Manteniendo la 5ª bien)

        // Umbral extra para que la marcha cambie "tarde" y haga clipping
        float clipThreshold = 0.04f;

        // --- LÓGICA DE MARCHAS SECUENCIAL Y DIRECCIÓN ---
        int targetGear = currentGear;
        if (shiftTimer <= 0) {
            // Cambio de Dirección / Arranque (Solo si estamos casi parados)
            if (absSpeed < 0.05) {
                if (inputForward && currentGear <= 0) {
                    targetGear = 1;
                } else if (inputBackward && currentGear >= 0) {
                    targetGear = -1;
                } else if (!inputForward && !inputBackward && absSpeed < 0.01) {
                    targetGear = 0; // Parking
                }
            }

            // Lógica de Progresión (Solo si ya estamos en avance)
            if (currentGear >= 1) {
                if (inputForward) {
                    if (currentGear == 1 && speedPercent > m1Max + clipThreshold)
                        targetGear = 2;
                    else if (currentGear == 2 && speedPercent > m2Max + clipThreshold)
                        targetGear = 3;
                    else if (currentGear == 3 && speedPercent > m3Max + clipThreshold)
                        targetGear = 4;
                    else if (currentGear == 4 && speedPercent > m4Max + clipThreshold)
                        targetGear = 5;
                }

                // Reducción secuencial (Ajustada para que no caiga de vueltas al reducir)
                if (currentGear == 5 && speedPercent < m4Max - 0.10)
                    targetGear = 4;
                else if (currentGear == 4 && speedPercent < m3Max - 0.10)
                    targetGear = 3;
                else if (currentGear == 3 && speedPercent < m2Max - 0.10)
                    targetGear = 2;
                else if (currentGear == 2 && speedPercent < m1Max - 0.08)
                    targetGear = 1;
            }
        }

        if (targetGear != currentGear && shiftTimer <= 0) {
            currentGear = targetGear;
            if (!this.getWorld().isClient) {
                this.dataTracker.set(GEAR, currentGear);
            }
            if (currentGear == 1)
                shiftTimer = 5;
            else if (currentGear == -1)
                shiftTimer = 12;
            else if (currentGear > 1)
                shiftTimer = 12; // Cambio normal entre marchas cortas
        }

        double targetSpeed = 0.0;
        // Compensación de Inercia: A más velocidad, usamos un lerp ligeramente más alto
        // para vencer la resistencia
        double speedBonus = (absSpeed / safeMaxSpeed) * 0.01;
        double lerpFactor = accelerationStat + speedBonus;

        if (shiftTimer > 0) {
            shiftTimer--;
            targetSpeed = forwardSpeed * 0.98; // Embrague más suave (mantiene más inercia)
            lerpFactor = 0.05;
            this.rpm = MathHelper.lerp(0.2f, this.rpm, 0.4f);
        } else {
            float targetRpm = 0.0f;

            // GESTIÓN DE ACELERACIÓN / FRENADO (Suave)
            if (currentGear >= 1) {
                if (inputForward) {
                    targetSpeed = maxSpeed;
                } else if (inputBackward) {
                    targetSpeed = 0;
                    lerpFactor = accelerationStat * 1.5f;
                }
            } else if (currentGear == -1) {
                if (inputBackward) {
                    targetSpeed = -maxSpeed * 0.4f;
                } else if (inputForward) {
                    targetSpeed = 0;
                    lerpFactor = accelerationStat * 1.5f;
                }
            }

            // Cálculo de RPM (Avance o Reversa)
            if ((currentGear >= 1 && inputForward) || (currentGear == -1 && inputBackward)) {
                float minS = 0, maxS = 1;
                if (currentGear == 1 || currentGear == -1) {
                    minS = 0.00f;
                    maxS = m1Max;
                } else if (currentGear == 2) {
                    minS = m1Max;
                    maxS = m2Max;
                } else if (currentGear == 3) {
                    minS = m2Max;
                    maxS = m3Max;
                } else if (currentGear == 4) {
                    minS = m3Max;
                    maxS = m4Max;
                } else if (currentGear == 5) {
                    minS = m4Max;
                    maxS = 1.00f;
                }

                targetRpm = (float) ((speedPercent - minS) / (maxS - minS));
                targetRpm = MathHelper.clamp(targetRpm, 0.0f, 1.2f);

                // --- LIMITADOR SUAVE (Sin frenazos bruscos) ---
                if (targetRpm > 1.0f && currentGear < 5 && currentGear != -1) {
                    targetRpm = 0.98f + (this.random.nextFloat() * 0.04f);
                    lerpFactor *= 0.7f; // Menos intrusivo (antes 0.4)
                }

                if (speedPercent > 0.98f) {
                    isLimiting = (this.age % 2 == 0);
                    targetRpm = 0.96f + (this.random.nextFloat() * 0.04f);
                    // En lugar de lerpFactor 0, usamos uno muy pequeño para mantener la punta sin
                    // vibración
                    targetSpeed = maxSpeed;
                    lerpFactor = 0.005;
                }
            }
            float idle = (getFuel() > 0) ? 0.1f : 0.0f;
            this.rpm = MathHelper.lerp(0.15f, this.rpm, Math.max(idle, targetRpm));
        }
        if (!this.getWorld().isClient) {
            this.dataTracker.set(RPM_SYNC, this.rpm);
        }
        // Reducir la aceleración durante el drift (al 50%)
        if (isDrifting) {
            lerpFactor *= 0.5;
        }

        // --- ACTUALIZACIÓN DE VELOCIDAD FINAL ---
        forwardSpeed = MathHelper.lerp(MathHelper.clamp(lerpFactor, 0, 1), forwardSpeed, targetSpeed);

        // Umbral de detención muy bajo para permitir que el tractor empiece a rodar
        if (Math.abs(forwardSpeed) < 0.001)
            forwardSpeed = 0;

        float speedRatio = (float) Math.min(1.0, absSpeed / safeMaxSpeed);
        // El giro cae un 85% a máxima velocidad (Subviraje natural)
        float baseSteerRate = (6.0f + grip * 2.5f) * (1.0f - (float) Math.pow(speedRatio, 1.2) * 0.85f);

        // ======================================================
        // --- DRIFT ESTILO MARIO KART ---
        // ======================================================

        // FASE 1: Gestión del Estado de Drift
        // - Entrada: Espacio + (A o D) con velocidad suficiente
        // - Dirección bloqueada una vez dentro (driftDir)
        // - Salida: al soltar Espacio
        boolean canStartDrift = inputJump && absSpeed > 0.12 && (inputLeft || inputRight);

        if (!isDrifting) {
            if (canStartDrift) {
                isDrifting = true;
                driftDir = inputLeft ? -1 : 1;
                driftAngle = 12.0f * driftDir; // Ángulo inicial ya abierto
                driftYawRate = 0.0f;
            }
        } else {
            if (!inputJump) { // Salir del drift al soltar Espacio
                isDrifting = false;
                driftDir = 0;
                driftAngle = 0.0f;
                driftYawRate = 0.0f;
            }
        }

        // --------------------------------------------------------
        // FASE 2: Steering normal (siempre activo)
        // --------------------------------------------------------
        float maxSteerRate = baseSteerRate;

        float steerTarget = 0;
        if (inputLeft)
            steerTarget = -maxSteerRate;
        if (inputRight)
            steerTarget = maxSteerRate;

        float steerLerp = isDrifting ? 0.30f : 0.25f;
        steering = steering + (steerTarget - steering) * steerLerp;

        if (!inputLeft && !inputRight)
            steering *= 0.6f;

        if (Math.abs(forwardSpeed) > 0.002) {
            // vehicleYaw += steering * Math.signum(forwardSpeed);
            // reduce el impacto del volante un 80% solo durante el drift:
            float steerFactor = isDrifting ? 0.8f : 1.0f;
            vehicleYaw += (steering * steerFactor) * Math.signum(forwardSpeed);
        } else if (inputLeft || inputRight) {
            vehicleYaw += steering * 0.4f;
        }

        // --------------------------------------------------------
        // FASE 3: Física de drift o movimiento normal
        // --------------------------------------------------------
        double yawRad = Math.toRadians(vehicleYaw);
        Vec3d currentVel = this.getVelocity();
        double finalVX, finalVZ;

        if (isDrifting) {
            // --- 3a: Apertura / cierre del ángulo de drift ---
            // Tecla contraria al driftDir → abre (giro más amplio, hasta ~50°)
            // Misma tecla que driftDir → cierra (giro más cerrado, hasta ~10°)
            // Sin tecla lateral → posición media neutra (~25°)
            boolean pressingWide = (driftDir == -1 && inputRight) || (driftDir == 1 && inputLeft);
            boolean pressingNarrow = (driftDir == -1 && inputLeft) || (driftDir == 1 && inputRight);

            float angleTarget;
            if (pressingWide)
                angleTarget = 50.0f * driftDir;
            else if (pressingNarrow)
                angleTarget = 10.0f * driftDir;
            else
                angleTarget = 25.0f * driftDir;

            driftAngle = MathHelper.lerp(0.06f, driftAngle, angleTarget);

            // --- 3b: Pivote adicional del morro por el drift ---
            // A menos velocidad, le damos un "boost" extra al pivote del drift para que
            // venza al volante
            float driftPowerFactor = isDrifting ? MathHelper.lerp((float) speedPercent,
                    2.0f, 1.0f) : 1.0f;
            float driftYawContrib = driftAngle * 0.04f * driftPowerFactor; // grados/tick extras
            vehicleYaw += driftYawContrib;
            yawRad = Math.toRadians(vehicleYaw);

            // --- 3c: Vector de velocidad = morro + deslizamiento lateral ---
            double noseVX = -Math.sin(yawRad) * forwardSpeed;
            double noseVZ = Math.cos(yawRad) * forwardSpeed;

            // Perpendicular al morro, hacia el exterior del drift
            double sideRad = yawRad + (Math.PI / 2.0) * (-driftDir);
            double lateralIntensity = Math.abs(driftAngle) / 50.0; // 0..1
            double sideSpeed = forwardSpeed * MathHelper.clamp(lateralIntensity * 0.55, 0.0, 0.55);
            double sideVX = -Math.sin(sideRad) * sideSpeed;
            double sideVZ = Math.cos(sideRad) * sideSpeed;

            double targetVX = noseVX + sideVX;
            double targetVZ = noseVZ + sideVZ;

            // Inercia baja → el deslizamiento lateral es muy notorio
            finalVX = MathHelper.lerp(0.20f, currentVel.x, targetVX);
            finalVZ = MathHelper.lerp(0.20f, currentVel.z, targetVZ);

            // El drift NO frena (rozamiento cosmético mínimo)
            forwardSpeed *= 0.9995;

        } else {
            // --- Movimiento normal (alta alineación con el morro) ---
            double targetVX = -Math.sin(yawRad) * forwardSpeed;
            double targetVZ = Math.cos(yawRad) * forwardSpeed;

            finalVX = MathHelper.lerp(0.96f, currentVel.x, targetVX);
            finalVZ = MathHelper.lerp(0.96f, currentVel.z, targetVZ);
        }

        this.setVelocity(finalVX, currentVel.y - 0.04, finalVZ);
        this.velocityModified = true;
        this.velocityDirty = true; // FORZAR PAQUETE DE VELOCIDAD PARA EVITAR STUTTER

        this.setYaw(vehicleYaw);
        this.setHeadYaw(vehicleYaw);
        this.setBodyYaw(vehicleYaw);
        this.dataTracker.set(SYNC_YAW, vehicleYaw);
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater updater) {
        if (this.hasPassenger(passenger)) {
            // Sincronización milimétrica de la posición del pasajero con el vehículo
            // Esto evita que al acelerar el jugador parezca "deslizarse" fuera del asiento
            double x = this.getX();
            double y = this.getY() + this.getMountedHeightOffset() + passenger.getHeightOffset();
            double z = this.getZ();

            updater.accept(passenger, x, y, z);

            // Forzar que el pasajero mire en la dirección del vehículo o mantenga su cabeza
            if (passenger instanceof PlayerEntity player) {
                // Opcional: podrías forzar el yaw del jugador aquí si quisieras
            }
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.getWorld().isClient) {
            // Marcamos el vehículo como sucio para forzar una sincronización de posición final
            // y evitar el "snapback" o teletransporte del jugador y el coche al bajar.
            this.velocityDirty = true;
            this.velocityModified = true;
        }
    }

    private void applyFriction(float multiplier) {
        forwardSpeed *= multiplier;
        double yawRad = Math.toRadians(vehicleYaw);
        this.setVelocity(-Math.sin(yawRad) * forwardSpeed, this.getVelocity().y - 0.08,
                Math.cos(yawRad) * forwardSpeed);
        this.velocityModified = true;
        this.dataTracker.set(SYNC_YAW, vehicleYaw);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient) {
            ItemStack held = player.getStackInHand(hand);
            if (held.isOf(ModItems.WRENCH)) {
                if (this.dataTracker.get(IS_DESTROYED)) {
                    // Reparar el vehículo destruido: drop de componentes y recursos, luego
                    // auto-destruir
                    repairOrBreak();
                    return ActionResult.SUCCESS;
                }
                player.openHandledScreen(this);
            } else if (held.isOf(ModItems.FUEL_CAN)) {
                setFuel(getFuel() + 25.0f);
                return ActionResult.SUCCESS;
            } else {
                if (!this.dataTracker.get(IS_DESTROYED)) {
                    player.startRiding(this);
                }
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }

    protected void repairOrBreak() {
        // Soltar todos los componentes del inventario
        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack s = this.inventory.getStack(i);
            if (!s.isEmpty()) {
                net.minecraft.entity.ItemEntity ie = new net.minecraft.entity.ItemEntity(
                        this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), s.copy());
                this.getWorld().spawnEntity(ie);
            }
        }
        // Soltar chatarra (3-5 hierros)
        net.minecraft.util.math.random.Random rand = this.getWorld().getRandom();
        int scrapCount = 3 + rand.nextInt(3);
        net.minecraft.entity.ItemEntity scrap = new net.minecraft.entity.ItemEntity(
                this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(),
                new ItemStack(net.minecraft.item.Items.IRON_INGOT, scrapCount));
        this.getWorld().spawnEntity(scrap);
        this.discard(); // Eliminar la entidad
    }

    public SimpleInventory getInventory() {
        return inventory;
    }

    public abstract ItemStack getEngineStack();

    public abstract ItemStack getWheelStack();

    public abstract float getBaseWeight();

    public int getColor() {
        return this.dataTracker.get(COLOR);
    }

    public void setColor(int color) {
        this.dataTracker.set(COLOR, color);
    }

    public float getAerodynamics() {
        return this.dataTracker.get(AERODYNAMICS);
    }

    public void setAerodynamics(float aero) {
        this.dataTracker.set(AERODYNAMICS, aero);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers
                .add(new software.bernie.geckolib.core.animation.AnimationController<>(this, "controller", 0, state -> {
                    if (Math.abs(forwardSpeed) > 0.01) {
                        return state.setAndContinue(software.bernie.geckolib.core.animation.RawAnimation.begin()
                                .thenLoop("animation.tractor.move"));
                    }
                    return state.setAndContinue(software.bernie.geckolib.core.animation.RawAnimation.begin()
                            .thenLoop("animation.tractor.idle"));
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public Text getDisplayName() {
        return this.getType().getName();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInv, PlayerEntity player) {
        return new com.gl.vehicles.gui.VehicleScreenHandler(syncId, playerInv, this.inventory, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeInt(this.inventory.size());
        buf.writeInt(this.getId());
    }

    public void setInputs(boolean f, boolean b, boolean l, boolean r, boolean j) {
        this.inputForward = f;
        this.inputBackward = b;
        this.inputLeft = l;
        this.inputRight = r;
        this.inputJump = j;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Color"))
            setColor(nbt.getInt("Color"));
        if (nbt.contains("Aero"))
            setAerodynamics(nbt.getFloat("Aero"));
        if (nbt.contains("Fuel"))
            setFuel(nbt.getFloat("Fuel"));
        if (nbt.contains("ChassisHealth"))
            this.dataTracker.set(CHASSIS_HEALTH, nbt.getFloat("ChassisHealth"));
        if (nbt.contains("IsDestroyed"))
            this.dataTracker.set(IS_DESTROYED, nbt.getBoolean("IsDestroyed"));

        net.minecraft.util.collection.DefaultedList<ItemStack> stacks = net.minecraft.util.collection.DefaultedList
                .ofSize(this.inventory.size(), ItemStack.EMPTY);
        net.minecraft.inventory.Inventories.readNbt(nbt, stacks);
        for (int i = 0; i < stacks.size(); i++) {
            this.inventory.setStack(i, stacks.get(i));
        }

        if (nbt.contains("VehicleYaw")) {
            this.vehicleYaw = nbt.getFloat("VehicleYaw");
            this.dataTracker.set(SYNC_YAW, this.vehicleYaw);
        }
        calculateStats();
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Color", getColor());
        nbt.putFloat("Aero", getAerodynamics());
        nbt.putFloat("Fuel", getFuel());
        nbt.putFloat("ChassisHealth", this.dataTracker.get(CHASSIS_HEALTH));
        nbt.putBoolean("IsDestroyed", this.dataTracker.get(IS_DESTROYED));

        net.minecraft.util.collection.DefaultedList<ItemStack> stacks = net.minecraft.util.collection.DefaultedList
                .ofSize(this.inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < this.inventory.size(); i++) {
            stacks.set(i, this.inventory.getStack(i));
        }
        net.minecraft.inventory.Inventories.writeNbt(nbt, stacks);

        nbt.putFloat("VehicleYaw", this.vehicleYaw);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }
}