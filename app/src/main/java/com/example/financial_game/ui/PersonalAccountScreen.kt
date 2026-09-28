package com.example.financial_game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial_game.R
import com.example.financial_game.data.GameSnapshot
import com.example.financial_game.domain.HairColour
import com.example.financial_game.domain.HairStyle
import com.example.financial_game.domain.PetAppearance
import com.example.financial_game.domain.PET_APPEARANCE_CHANGE_PRICE
import com.example.financial_game.ui.theme.NunitoFontFamily

private val AccountPurple = Color(0xFF8743D3)
private val AccountPurpleDark = Color(0xFF4B2163)
private val AccountLavender = Color(0xFFF3DEFC)
private val AccountPageTop = Color(0xFFF8E9FF)
private const val MAX_PET_NAME_LENGTH = 20

private enum class AppearanceSection { Colour, Fur }

@Composable
internal fun PersonalAccountScreen(
    initialState: GameSnapshot,
    onBack: () -> Unit,
    onApply: (PetAppearance) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf(initialState.name) }
    var hairColour by rememberSaveable { mutableStateOf(initialState.hairColour) }
    var hairStyle by rememberSaveable { mutableStateOf(initialState.hairStyle) }
    var section by rememberSaveable { mutableStateOf(AppearanceSection.Fur) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to AccountPageTop,
                    0.43f to Color.White,
                    1f to Color.White,
                ),
            )
            .safeDrawingPadding()
            .padding(horizontal = 15.dp),
    ) {
        AccountTopBar(onBack)

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Image(
                painter = painterResource(R.drawable.pet_main),
                contentDescription = stringResource(R.string.pet),
                modifier = Modifier.width(225.dp).height(294.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Text(
            text = stringResource(R.string.personal_account_name),
            color = AccountPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(7.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { value ->
                if (value.length <= MAX_PET_NAME_LENGTH) name = value
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = AccountPurpleDark,
                fontFamily = NunitoFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(11.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccountPurple,
                unfocusedBorderColor = AccountPurple,
                cursorColor = AccountPurple,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
        )

        Spacer(Modifier.height(28.dp))
        AppearanceTabs(selected = section, onSelected = { section = it })
        Spacer(Modifier.height(22.dp))
        AppearanceChoices(
            section = section,
            hairColour = hairColour,
            hairStyle = hairStyle,
            onHairColourSelected = { hairColour = it },
            onHairStyleSelected = { hairStyle = it },
        )

        Spacer(Modifier.height(64.dp))
        Button(
            onClick = {
                focusManager.clearFocus()
                onApply(PetAppearance(name, hairColour, initialState.eyeColour, hairStyle))
            },
            enabled = name.isNotBlank() && initialState.money >= PET_APPEARANCE_CHANGE_PRICE,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(11.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccountPurple,
                disabledContainerColor = AccountPurple.copy(alpha = 0.3f),
            ),
        ) {
            Text(
                text = stringResource(R.string.apply_action),
                fontFamily = NunitoFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = PET_APPEARANCE_CHANGE_PRICE.toString(),
                fontFamily = NunitoFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.width(4.dp))
            Image(
                painter = painterResource(R.drawable.coin_money),
                contentDescription = stringResource(R.string.coins),
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(15.dp))
    }
}

@Composable
private fun AccountTopBar(onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(64.dp)) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart).size(40.dp),
        ) {
            Canvas(Modifier.size(20.dp)) {
                drawLine(
                    color = AccountPurpleDark,
                    start = Offset(size.width * 0.68f, size.height * 0.15f),
                    end = Offset(size.width * 0.32f, size.height * 0.5f),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = AccountPurpleDark,
                    start = Offset(size.width * 0.32f, size.height * 0.5f),
                    end = Offset(size.width * 0.68f, size.height * 0.85f),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        Text(
            text = stringResource(R.string.personal_account_title),
            modifier = Modifier.align(Alignment.Center),
            color = AccountPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun AppearanceTabs(
    selected: AppearanceSection,
    onSelected: (AppearanceSection) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(AccountLavender),
    ) {
        AppearanceSection.entries.forEach { item ->
            val selectedItem = item == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (selectedItem) AccountPurple else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelected(item) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(item.titleRes()),
                    color = if (selectedItem) Color.White else AccountPurpleDark,
                    fontFamily = NunitoFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun AppearanceChoices(
    section: AppearanceSection,
    hairColour: HairColour,
    hairStyle: HairStyle,
    onHairColourSelected: (HairColour) -> Unit,
    onHairStyleSelected: (HairStyle) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        when (section) {
            AppearanceSection.Colour -> HairColour.entries.forEach { colour ->
                AppearanceTile(
                    selected = colour == hairColour,
                    onClick = { onHairColourSelected(colour) },
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colour.displayColour()),
                    )
                }
            }
            AppearanceSection.Fur -> HairStyle.entries.forEach { style ->
                AppearanceTile(
                    selected = style == hairStyle,
                    onClick = { onHairStyleSelected(style) },
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                ) {
                    Image(
                        painter = painterResource(R.drawable.pet_main),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(style.previewPadding()),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppearanceTile(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(AccountLavender)
            .then(
                if (selected) Modifier.border(1.5.dp, AccountPurple, RoundedCornerShape(11.dp))
                else Modifier,
            )
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

private fun AppearanceSection.titleRes(): Int = when (this) {
    AppearanceSection.Colour -> R.string.personal_account_colour
    AppearanceSection.Fur -> R.string.personal_account_fur
}

private fun HairColour.displayColour(): Color = when (this) {
    HairColour.Beige -> Color(0xFFFFDFAE)
    HairColour.Violet -> Color(0xFFC8C1F7)
    HairColour.Orange -> Color(0xFFF4C4AA)
}

private fun HairStyle.previewPadding() = when (this) {
    HairStyle.Default -> 8.dp
    HairStyle.Hairy -> 3.dp
    HairStyle.Curly -> 12.dp
}
