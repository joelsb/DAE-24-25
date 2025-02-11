<template>
    <div>
        <!-- NavBar -->
        <NavBar />

        <!-- volume Table Section -->
        <div class="max-w-4xl mx-auto mt-6 p-5 bg-white rounded-lg shadow-md">
            <h2 class="text-2xl font-semibold mb-4">Volume Page</h2>
            <div class="mb-4 flex-row justify-between flex items-center">
                <span class=" text-lg text-gray-600">See all the volumes.</span>

                <button @click="router.go(-1)"
                    class="px-6 py-2 bg-blue-500 text-white rounded-full hover:bg-blue-600 transition">
                  🔙 Back
                </button>
            </div>
            <!-- Loading Indicator -->
            <div v-if="loading" class="flex justify-center items-center">
                <svg class="animate-spin h-8 w-8 text-gray-600" xmlns="http://www.w3.org/2000/svg" fill="none"
                    viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
                </svg>
            </div>

            <!-- Error Message -->
            <div v-if="error" class="text-red-500 text-center mb-4">{{ error }}</div>

            <!-- volumes Table -->
            <div v-if="!loading && !error" class="table-container">
                <div v-if="volumes.length === 0" class="text-center text-gray-500">
                    No volumes yet
                </div>
                <table v-if="volumes.length > 0" aria-label="volumes table" class="table w-full">
                    <thead>
                        <tr>
                            <th class="p-3 font-semibold text-left">Volume ID</th>
                            <th class="p-3 font-semibold text-left">Order ID</th>
                            <th class="p-3 font-semibold text-left">Volume Created Date</th>
                            <th class="p-3 font-semibold text-left">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="volume in paginatedVolumes" :key="volume.id">
                            <td class="p-3">
                                <button @click="viewvolumeDetails(volume.id)" class="text-blue-600 hover:underline">
                                    {{ volume.id }}
                                </button>
                            </td>
                            <td class="p-3">{{ volume.orderId }}</td>
                            <td class="p-3">{{ new Date(volume.sentDate).toLocaleString() }}</td>
                            <td class="p-3">
                                {{ volume.deliveredDate ? 'Entregue' : 'Por entregar' }}
                            </td>

                        </tr>
                    </tbody>
                </table>

                <!-- Pagination -->
                <div v-if="volumes.length > 0" class="flex justify-between items-center mt-4">
                    <button @click="prevPage" :disabled="currentPage === 1"
                        class="px-3 py-1 bg-gray-200 rounded disabled:opacity-50">
                        Previous
                    </button>
                    <span>Page {{ currentPage }}</span>
                    <button @click="nextPage" :disabled="currentPage === totalPages"
                        class="px-3 py-1 bg-gray-200 rounded disabled:opacity-50">
                        Next
                    </button>
                </div>
            </div>
        </div>
    </div>

</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRuntimeConfig } from '#imports';

import NavBar from '@/components/NavBar.vue'; // Import NavBar component
import { useRouter } from 'vue-router'; // Import useRouter apenas uma vez

const router = useRouter(); // Mantenha apenas uma declaração de 'router'

// API Config
const config = useRuntimeConfig();
const apiUrl = config.public.API_URL;

// Reactive Data
const volumes = ref([]);
const loading = ref(false);
const error = ref(null);
const currentPage = ref(1);
const pageSize = 10;

// Computed Paginated volumes
const paginatedVolumes = computed(() =>
    volumes.value.slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize)
);

// Total Pages
const totalPages = computed(() => Math.ceil(volumes.value.length / pageSize));

// Função para ver os detalhes do pedido e redirecionar
const viewvolumeDetails = (volumeId) => {
    console.log("Navigating to volumeDetails with id:", volumeId);  // Verifique se o id está correto
    router.push({ name: 'volume-id', params: { id: volumeId } });
};



// Fetch volumes Function
const fetchvolumes = async () => {
    loading.value = true;
    error.value = null;
    try {
        const response = await fetch(`${apiUrl}/volumes`);
        if (!response.ok) {
            throw new Error(`Failed to fetch: ${response.statusText}`);
        }
        volumes.value = await response.json();
        console.log("volumes", volumes.value);
    } catch (err) {
        error.value = err.message;
        console.error(err);
    } finally {
        loading.value = false;
    }
};

// Pagination Functions
const nextPage = () => {
    if (currentPage.value < totalPages.value) {
        currentPage.value++;
    }
};

const prevPage = () => {
    if (currentPage.value > 1) {
        currentPage.value--;
    }
};

// Fetch Data on Mount
onMounted(() => {
    fetchvolumes();
});
</script>


<style scoped>
.table-container {
    overflow-x: auto;
}

.table {
    border-collapse: collapse;
    width: 100%;
}

.table th,
.table td {
    border: 1px solid #ccc;
    padding: 0.75rem;
}

.table th {
    background-color: #f9f9f9;
}

.table td {
    text-align: left;
}
</style>
