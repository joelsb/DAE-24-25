<template>
    <div class="card shadow-lg p-6 rounded-lg bg-white">

        <div class="card-body">
            <div class="mb-6 flex justify-between items-center">
                <button @click="router.go(-1)" class="px-6 py-2 bg-blue-600 text-white rounded-full hover:bg-blue-700 transition-all ease-in-out">
                    🔙 Back
                </button>

                <!-- Edit Mode Toggle Switch -->
                <div v-if="!isCreate" class="flex justify-end items-center">
                    <label for="editProfileSwitch" class="text-lg font-semibold mr-4">🖋️ Edit Mode</label>
                    <label for="editProfileSwitch" class="inline-flex relative items-center cursor-pointer">
                        <input type="checkbox" id="editProfileSwitch" v-model="isEditing" class="sr-only" />
                        <div class="w-14 h-7 rounded-full transition-colors"
                            :class="isEditing ? 'bg-blue-500' : 'bg-gray-300'"></div>
                        <div class="absolute left-1 top-1 w-5 h-5 bg-white rounded-full transition-transform"
                            :class="isEditing ? 'transform translate-x-7' : ''"></div>
                    </label>
                    <span class="ml-4 text-lg">{{ isEditing ? '📝 Editing' : '👀 View' }}</span>
                </div>
            </div>

            <form class="form space-y-6" @submit.prevent="isCreate ? createProfile() : updateProfile()">
                
                <!-- Role -->
                <div class="form-group">
                    <label for="role" class="text-lg font-medium">🧑‍💼 Role</label>
                    <select id="role" v-model="form.role" class="form-control rounded-lg border-gray-300 w-full p-3 transition-all ease-in-out" :disabled="!isEditing || !isCreate">
                        <option v-if="form.role && !isCreate" :value="form.role">{{ form.role }}</option>
                        <option v-for="role in roles" :key="role" :value="role">{{ role }}</option>
                    </select>
                </div>

                <!-- Username -->
                <div class="form-group">
                    <label for="username" class="text-lg font-medium">👤 Username</label>
                    <input type="text" id="username" v-model="form.username" class="form-control rounded-lg border-gray-300 w-full p-3 transition-all ease-in-out" :disabled="!isEditing || !isCreate" />
                </div>

                <!-- Password (if isCreate) -->
                <div v-if="isCreate" class="form-group">
                    <label for="password" class="text-lg font-medium">🔒 Password</label>
                    <input type="password" id="password" v-model="form.password" class="form-control rounded-lg border-gray-300 w-full p-3 transition-all ease-in-out" :disabled="!isEditing || !isCreate" />
                </div>

                <!-- Email -->
                <div class="form-group">
                    <label for="email" class="text-lg font-medium">📧 Email</label>
                    <input type="email" id="email" v-model="form.email" class="form-control rounded-lg border-gray-300 w-full p-3 transition-all ease-in-out" :disabled="!isEditing" />
                </div>

                <!-- Name -->
                <div class="form-group">
                    <label for="name" class="text-lg font-medium">📝 Name</label>
                    <input type="text" id="name" v-model="form.name" class="form-control rounded-lg border-gray-300 w-full p-3 transition-all ease-in-out" :disabled="!isEditing" />
                </div>

                <!-- Office for Manager -->
                <div v-if="userRole === 'Manager'" class="form-group">
                    <label for="office" class="text-lg font-medium">🏢 Office</label>
                    <input type="text" id="office" v-model="form.office" class="form-control rounded-lg border-gray-300 w-full p-3 transition-all ease-in-out" :disabled="!isEditing" />
                </div>

                <!-- Warehouse for Employee -->
                <div v-if="userRole === 'Employee'" class="form-group">
                    <label for="warehouse" class="text-lg font-medium">🏭 Warehouse</label>
                    <input type="text" id="warehouse" v-model="form.warehouse" class="form-control rounded-lg border-gray-300 w-full p-3 transition-all ease-in-out" :disabled="!isEditing" />
                </div>

                <!-- Messages -->
                <div v-if="messages.length" class="form-messages space-y-2">
                    <ul>
                        <li v-for="(message, index) in messages" :key="index" class="message-item bg-red-100 p-3 rounded-lg text-red-600">
                            ⚠️ {{ message }}
                        </li>
                    </ul>
                </div>

                <!-- 💾 Save Button (Only in Edit Mode) -->
                <div v-if="isEditing" class="edit-button">
                    <button type="submit" class="bg-green-500 text-white py-3 px-6 rounded-full hover:bg-green-600 transition-all ease-in-out">
                        {{ isCreate ? '🆕 Create ' + form.role : '💾 Save changes' }}
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup>
import { useRuntimeConfig } from '#imports';
import { ref, defineProps, defineEmits, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '~/store/auth-store';

const config = useRuntimeConfig();
const apiUrl = config.public.API_URL;

const props = defineProps({
    userData: Object, // Initial data
    editProfile: Boolean,
    create: Boolean // New prop to handle user creation
});

const userRole = ref(null);
const isEditing = ref(props.editProfile);

const isCreate = ref(props.create); // Flag to determine if we're creating a new user

const roles = ref([]); // To store the fetched roles
const emit = defineEmits(["updateProfile", "createProfile"]);

const router = useRouter();
const user = useAuthStore().user;
const messages = ref([]);

const form = ref({ ...props.userData });

// Fetch available roles from the API
onMounted(async () => {
    if (!isCreate.value) return; // Don't fetch roles if we're not creating a new user
    try {
        const response = await fetch(`${apiUrl}/users/roles`);
        const data = await response.json();
        userRole.value = data.roles[0];
        form.value.role = userRole; // Set the role to the user's role by default
        roles.value = data.roles; // Assuming the API returns a list of roles
    } catch (error) {
        console.error('Error fetching roles:', error);
    }
});

// Watch for changes in the passed userData and create prop
watch(() => props.userData, (newData) => {
    form.value = { ...newData };
}, { deep: true });

watch(() => props.create, (newCreate) => {
    isCreate.value = newCreate;
    if (newCreate) {
        form.value.role = ''; // Ensure role is empty when creating a user
        form.value.username = ''; // Ensure username is empty when creating a user
    }
});

watch(() => props.editProfile, (newEditProfile) => {
    isEditing.value = newEditProfile;
});

function updateProfile() {
    emit("updateProfile", form.value);
}

function createProfile() {
    emit("createProfile", form.value);
}

</script>

<style scoped>
.form {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.card-body {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.edit-button {
    display: flex;
    justify-content: flex-end;
}

.card {
    max-width: 600px;
    margin: 2rem auto;
    padding: 1.5rem;
    border-radius: 0.5rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    background-color: #ffffff;
}

.card-header h2 {
    text-align: center;
}

.form-control {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #ddd;
    border-radius: 0.375rem;
    font-size: 1rem;
}

.form-messages ul {
    color: red;
    list-style: none;
    padding: 0;
    margin-top: 1rem;
}
</style>
