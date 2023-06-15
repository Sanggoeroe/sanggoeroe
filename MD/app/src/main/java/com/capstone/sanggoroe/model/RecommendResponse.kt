package com.capstone.sanggoroe.model

import com.google.gson.annotations.SerializedName

data class RecommendResponse(

	@field:SerializedName("RecommendResponse")
	val recommendResponse: List<RecommendResponseItem>
)

data class RecommendResponseItem(

	@field:SerializedName("Kota")
	val kota: String,

	@field:SerializedName("Similarity")
	val similarity: Any,

	@field:SerializedName("Jenjang")
	val jenjang: String,

	@field:SerializedName("Position")
	val position: String,

	@field:SerializedName("Kualifikasi")
	val kualifikasi: String,

	@field:SerializedName("Deskripsi")
	val deskripsi: String,

	@field:SerializedName("JobID")
	val jobID: Int
)
