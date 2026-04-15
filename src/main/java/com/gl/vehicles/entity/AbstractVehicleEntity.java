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
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
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

    public static final float MAX_CHASSIS_HEALTH = 300.0f;

    protected final SimpleInventory inventory;
    protected float steering = 0.0f;
    protected boolean inputForward, inputBackward, inputLeft, inputRight;

    protected double forwardSpeed = 0.0;
    protected float vehicleYaw = 0.0f;
    protected float accelerationStat = 0.0f;
    protected float maxSpeed = 0.0f;
    protected float grip = 1.0f;
    protected float weight = 100.0f;

    // --- SISTEMA DE MARCHAS ---
    protected int currentGear = 1;
    protected int shiftTimer = 0;
    protected float rpm = 0.0f;
    protected boolean isLimiting = false;

    // --- SISTEMA DE DAÑO ---
    protected Vec3d prevVelocityForCollision = Vec3d.ZERO;
    protected int smokeTimer = 0;
    protected int destroyedRepairCooldown = 0; // Cooldown de la llave inglesa al reparar

    public AbstractVehicleEntity(EntityType<? extends AbstractVehicleEntity> type, World world, int inventorySize) {
        super(type, world);
        this.inventory = new SimpleInventory(inventorySize);
        this.inventory.addListener(sender -> this.calculateStats());
        this.vehicleYaw = this.getYaw();
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
    }

    // BAJAR AL JUGADOR (Estaba muy alto)
    @Override
    public double getMountedHeightOffset() {
        return 0.34D; // Ajusta este valor si sigue flotando (antes era por defecto ~1.0)
    }

    @Override
    public float getYaw(float tickDelta) {
        return this.dataTracker.get(SYNC_YAW);
    }

    @Override
    public float getHeadYaw() {
        return this.dataTracker.get(SYNC_YAW);
    }

    @Override
    public float getBodyYaw() {
        return this.dataTracker.get(SYNC_YAW);
    }

    public float getFuel() {
        return this.dataTracker.get(FUEL);
    }

    public void setFuel(float fuel) {
        this.dataTracker.set(FUEL, Math.max(0, Math.min(100.0f, fuel)));
    }

    public void calculateStats() {
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

        if (hasEngine && hasWheels) {
            this.grip = Math.max(0.1f, (tireGrip / tireWear));
            // Tractor pesado -> menos aceleración
            this.accelerationStat = (0.04f * enginePower);
            this.maxSpeed = 0.45f * enginePower;
        } else {
            this.grip = 0.0f;
            this.accelerationStat = 0.0f;
            this.maxSpeed = 0.0f;
            this.forwardSpeed = 0.0;
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
        if (this.getWorld().isClient) {
            this.vehicleYaw = this.dataTracker.get(SYNC_YAW);
            this.setYaw(this.vehicleYaw);
            this.prevYaw = this.vehicleYaw;
            this.setHeadYaw(this.vehicleYaw);
            this.setBodyYaw(this.vehicleYaw);

            // --- SONIDO DEL MOTOR (cliente) ---
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
                    net.minecraft.sound.SoundEvents.BLOCK_FIRE_EXTINGUISH, // placeholder hasta custom sound
                    net.minecraft.sound.SoundCategory.NEUTRAL,
                    1.0f, pitch, false // Subido a 1.0f
                );
            }

            // --- PARTICULAS EN CLIENTE ---
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
                        0, 0.04, 0
                    );
                }
            }
        }

        super.tick();
        if (this.getWorld().isClient)
            return;

        // --- COLISION POR VELOCIDAD (servidor) ---
        Vec3d curVel = this.getVelocity();
        double speedKmH = prevVelocityForCollision.horizontalLength() * 72.0;
        if (speedKmH > 30.0) {
            // Detectar cambio brusco de velocidad horizontal = choque contra bloque
            double speedDelta = prevVelocityForCollision.horizontalLength() - curVel.horizontalLength();
            if (speedDelta > 0.15) { // Frenada muy brusca: choque
                float damage = (float) (speedDelta * 30.0); // Mucho más permisivo (antes 80.0)
                applyChassisHit(damage);
                
                // Dañar al jugador para que salgan los bordes rojos
                if (this.getFirstPassenger() instanceof PlayerEntity player) {
                    player.damage(this.getWorld().getDamageSources().generic(), damage / 5.0f);
                }
            }
        }
        prevVelocityForCollision = curVel;

        boolean destroyed = this.dataTracker.get(IS_DESTROYED);
        boolean canMove = !destroyed && !getEngineStack().isEmpty() && !getWheelStack().isEmpty() && getFuel() > 0;

        if (this.hasPassengers() && canMove) {
            handlePhysics();
            if (Math.abs(forwardSpeed) > 0.01) {
                float fuelUsage = 0.005f;
                ItemStack engineStack = getEngineStack();
                if (engineStack.getItem() instanceof com.gl.vehicles.item.EngineItem engine) {
                    fuelUsage *= engine.getFuelConsumption();
                }
                setFuel(getFuel() - fuelUsage);
                applyWear(getEngineStack(), 0.00001f);
                applyWear(getWheelStack(), 0.00002f);
            }
        } else {
            forwardSpeed = 0;
            applyFriction(0.85f);
        }
        this.move(MovementType.SELF, this.getVelocity());

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
            // Spawn de humo inmediato (se hace en cliente por particulas)
            if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                sw.spawnParticles(net.minecraft.particle.ParticleTypes.LARGE_SMOKE,
                    this.getX(), this.getY() + 0.5, this.getZ(), 20, 0.5, 0.5, 0.5, 0.02);
            }
        }
    }

    public float getChassisHealth() { return this.dataTracker.get(CHASSIS_HEALTH); }
    public boolean isDestroyed()    { return this.dataTracker.get(IS_DESTROYED); }

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
        double speedPercent = maxSpeed > 0 ? (forwardSpeed / maxSpeed) : 0;
        
        // 5 Marchas basadas en porcentaje de velocidad máxima
        int targetGear = 1;
        if (speedPercent > 0.85) targetGear = 5;
        else if (speedPercent > 0.60) targetGear = 4;
        else if (speedPercent > 0.35) targetGear = 3;
        else if (speedPercent > 0.15) targetGear = 2;
        
        if (forwardSpeed < 0.01) targetGear = 0;

        if (targetGear != currentGear && shiftTimer <= 0 && targetGear > 0) {
            currentGear = targetGear;
            this.dataTracker.set(GEAR, currentGear);
            shiftTimer = 6; 
        }

        double targetSpeed = 0.0;
        double lerpFactor = (0.06 * getAerodynamics());
        isLimiting = false;

        if (shiftTimer > 0) {
            shiftTimer--;
            targetSpeed = forwardSpeed * 0.92; // Embrague
            lerpFactor = 0.15;
        } else {
            if (inputForward) {
                targetSpeed = maxSpeed;
                lerpFactor = accelerationStat;
                
                // RPM Dinámicas para la barra (entre 0.0 y 1.0)
                float gearRange = 0.25f;
                float rpmVal = (float)((speedPercent % gearRange) / gearRange);
                this.dataTracker.set(RPM_SYNC, rpmVal);

                if (speedPercent > 0.98f) {
                    isLimiting = (this.age % 2 == 0);
                    if (isLimiting) targetSpeed = forwardSpeed * 0.85;
                }
            } else {
                this.dataTracker.set(RPM_SYNC, 0.0f);
            }
            if (inputBackward) {
                targetSpeed = -maxSpeed * 0.55;
                lerpFactor = accelerationStat;
            }
        }

        lerpFactor = Math.min(1.0, Math.max(0.001, lerpFactor));
        forwardSpeed = net.minecraft.util.math.MathHelper.lerp(lerpFactor, forwardSpeed, targetSpeed);

        if (Math.abs(forwardSpeed) < 0.001)
            forwardSpeed = 0.0;

        float speedRatio = (float) Math.min(1.0, Math.abs(forwardSpeed) / Math.max(0.001, maxSpeed));
        // El giro base ahora escala con el Grip (más grip = giro más cerrado)
        float maxSteerRate = (6.0f + grip * 2.0f) * (1.0f - speedRatio * 0.5f);

        float steerTarget = 0;
        if (inputLeft)
            steerTarget = -maxSteerRate;
        if (inputRight)
            steerTarget = maxSteerRate;
        steering = steering + (steerTarget - steering) * 0.25f;
        if (!inputLeft && !inputRight)
            steering *= 0.6f;

        if (Math.abs(forwardSpeed) > 0.002) {
            vehicleYaw += steering * Math.signum(forwardSpeed);
        } else if (inputLeft || inputRight) {
            vehicleYaw += steering * 0.4f;
        }

        double yawRad = Math.toRadians(vehicleYaw);
        this.setVelocity(-Math.sin(yawRad) * forwardSpeed, this.getVelocity().y - 0.08,
                Math.cos(yawRad) * forwardSpeed);
        this.velocityModified = true;

        this.setYaw(vehicleYaw);
        this.setHeadYaw(vehicleYaw);
        this.setBodyYaw(vehicleYaw);
        this.dataTracker.set(SYNC_YAW, vehicleYaw);
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
                    // Reparar el vehículo destruido: drop de componentes y recursos, luego auto-destruir
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
        return new VehicleScreenHandler(syncId, playerInv, this.inventory, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeInt(this.inventory.size());
        buf.writeInt(this.getId());
    }

    public void setInputs(boolean f, boolean b, boolean l, boolean r) {
        this.inputForward = f;
        this.inputBackward = b;
        this.inputLeft = l;
        this.inputRight = r;
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

        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(this.inventory.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, stacks);
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
        nbt.putFloat("VehicleYaw", this.dataTracker.get(SYNC_YAW));
        nbt.putFloat("ChassisHealth", this.dataTracker.get(CHASSIS_HEALTH));
        nbt.putBoolean("IsDestroyed", this.dataTracker.get(IS_DESTROYED));

        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(this.inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < this.inventory.size(); i++) {
            stacks.set(i, this.inventory.getStack(i));
        }
        Inventories.writeNbt(nbt, stacks);
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
