<template>

<v-data-table
    :headers="headers"
    :items="reservationview"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'Reservationview',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "roomType", value: "roomType" },
            { text: "state", value: "state" },
            { text: "price", value: "price" },
            { text: "roomId", value: "roomId" },
            { text: "paymentId", value: "paymentId" },
        ],
        reservationview : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/reservationviews')

      this.reservationview = temp.data._embedded.reservationviews;

    },
    methods: {
    }
  }
</script>

