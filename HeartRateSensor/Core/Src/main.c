/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file           : main.c
  * @brief          : Main program body
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; Copyright (c) 2021 STMicroelectronics.
  * All rights reserved.</center></h2>
  *
  * This software component is licensed by ST under BSD 3-Clause license,
  * the "License"; You may not use this file except in compliance with the
  * License. You may obtain a copy of the License at:
  *                        opensource.org/licenses/BSD-3-Clause
  *
  ******************************************************************************
  */
/* USER CODE END Header */
/* Includes ------------------------------------------------------------------*/
#include "main.h"

/*  NOTES:
 /  Interrupt enable register - enable1: bit 6. ppg_ready_en means sample is ready
 /  Want to use this to trigger an interrupt here to read data
 /  Read data through I2C
 /
 /  NEED:
 /  Initialize LED function
 /  Initialize/Declare interrupt handlers
 /  Initialize UART
 /  Set up I2C write for chip initialization
 /  Set up I2C read for data (heart rate samples)
 /  Set up EXTI interrupt on pin that's connected to ppg_ready on chip
 /  Set up initialize timer to send out UART at set intervals
 /
 /  IF WE HAVE TIME:
 /  DAC for output based on heart rate
 /  Blinking LED at same rate as heart rate
*/

/* Private function prototypes -----------------------------------------------*/
void SystemClock_Config(void);
/* USER CODE BEGIN PFP */
void transmitChar (char c);
void transmitString (char c[]);
void initializeClocks (void);
void initializeLEDs (void);
void initializeUART (void);
void initializeHeartRateChip (void);
void setUpInterrupts (void);
void initializeI2C (void);
void setUpTimer (void);
void EXTI4_15_IRQHandler(void);
void TIM2_IRQHandler(void);
void restartWriteCondition(uint16_t);
void restartReadCondition(uint16_t);
void waitForTxis(void);
void waitForTC(void);
void WaitForRXNEorNACKF(void);
volatile uint32_t _sample;
volatile uint32_t counter = 0;



/* USER CODE END PFP */

/* Private user code ---------------------------------------------------------*/
/* USER CODE BEGIN 0 */

/* USER CODE END 0 */

/**
  * @brief  The application entry point.
  * @retval int
  */
int main(void)
{
  /* USER CODE BEGIN 1 */

  /* USER CODE END 1 */

  /* MCU Configuration--------------------------------------------------------*/

  /* Reset of all peripherals, Initializes the Flash interface and the Systick. */
  HAL_Init();

  /* USER CODE BEGIN Init */
	initializeClocks();
	initializeLEDs();
	initializeI2C();
	//initializeUART();
	initializeHeartRateChip();
	setUpInterrupts();
	//setUpTimer();
  /* USER CODE END Init */

  /* Configure the system clock */
  SystemClock_Config();
	
  while (1)
  {
	
  }
  /* USER CODE END 3 */
}

/**
  * @brief System Clock Configuration
  * @retval None
  */
void SystemClock_Config(void)
{
  RCC_OscInitTypeDef RCC_OscInitStruct = {0};
  RCC_ClkInitTypeDef RCC_ClkInitStruct = {0};

  /** Initializes the RCC Oscillators according to the specified parameters
  * in the RCC_OscInitTypeDef structure.
  */
  RCC_OscInitStruct.OscillatorType = RCC_OSCILLATORTYPE_HSI;
  RCC_OscInitStruct.HSIState = RCC_HSI_ON;
  RCC_OscInitStruct.HSICalibrationValue = RCC_HSICALIBRATION_DEFAULT;
  RCC_OscInitStruct.PLL.PLLState = RCC_PLL_NONE;
  if (HAL_RCC_OscConfig(&RCC_OscInitStruct) != HAL_OK)
  {
    Error_Handler();
  }
  /** Initializes the CPU, AHB and APB buses clocks
  */
  RCC_ClkInitStruct.ClockType = RCC_CLOCKTYPE_HCLK|RCC_CLOCKTYPE_SYSCLK
                              |RCC_CLOCKTYPE_PCLK1;
  RCC_ClkInitStruct.SYSCLKSource = RCC_SYSCLKSOURCE_HSI;
  RCC_ClkInitStruct.AHBCLKDivider = RCC_SYSCLK_DIV1;
  RCC_ClkInitStruct.APB1CLKDivider = RCC_HCLK_DIV1;

  if (HAL_RCC_ClockConfig(&RCC_ClkInitStruct, FLASH_LATENCY_0) != HAL_OK)
  {
    Error_Handler();
  }
}

/* USER CODE BEGIN 4 */
void initializeClocks(){
	/* USER CODE BEGIN SysInit */
	RCC->AHBENR |= (1 << 19) | (1 << 18);
	RCC->APB1ENR |= RCC_APB1ENR_I2C2EN;
	RCC->APB1ENR |=  RCC_APB1ENR_USART3EN;
	RCC->APB2ENR |= (1 << 0);
	RCC->APB1ENR |= RCC_APB1ENR_TIM2EN | RCC_APB1ENR_TIM3EN;
}

void initializeLEDs(){
		// LED initialization
	GPIOC->MODER |= (1 << 12) | (1 << 14) | (1 << 16) | (1 << 18);
  GPIOC->OTYPER |= (0 << 6) | (0 << 7) | (0 << 8) | (0 << 9);
  GPIOC->OSPEEDR |= (1 << 13) | (1 << 15) | (1 << 17) | (1 << 19);
  GPIOC->PUPDR &= 0;
}

void initializeI2C(){
	// PB11 and PB13 and PB14
	GPIOB->MODER |= (1 << 23) | (1 << 27) | (1 << 28);
	GPIOB->OTYPER |= (1 << 11) | (1 << 13);
	//GPIOB->OTYPER &= ~(1 << 14);
	GPIOB->AFR[1] |= (1 << 12) | (1 << 22) | (1 << 20);
	GPIOB->ODR |= (1 << 14);
	GPIOB->PUPDR |= (1 << 22) | (1 << 26);
	
	// PC0
	GPIOC->MODER |= (1 << 0);
	GPIOC->OTYPER &= ~(1 << 0);
	GPIOC->ODR |= (1 << 0);
		
	// Using 100kHz standard-mode as defined by figure 5.4 in lab manual
	I2C2->TIMINGR |= (1 << 28) | (0xF << 8) | (0x13 << 0) | (0x2 << 16) | (0x4 << 20);
	
	// Enable the peripheral
	I2C2->CR1 |= (1 << 0);
	//| (1 << 0) | (1 << 4) | (1 << 6) | (1 << 7);
}

void initializeUART(){
	GPIOB->MODER |= (1 << 9) | (1 << 11);
	GPIOB->MODER &= ~((1 << 8) | (1 << 10));
	
	
	GPIOB->AFR[0] |= (1 << 18) | (1 << 22);
	GPIOB->AFR[0] &= ~((1 << 23) | (1 << 21) | (1 << 20) | (1 << 19) | (1 << 17) | (1 << 16));
		
	uint32_t freq = HAL_RCC_GetHCLKFreq();
	USART3->BRR |= (freq / 115200);
	USART3->CR1 |= (1 << 2) | (1 << 3);
	USART3->CR1 |= (1 << 0) | (1 << 5);
}

void initializeHeartRateChip(){
	// Initialize chip to heart rate mode
	// Slave address is 0xBE, number of bytes to transmit is 2, write operation, start bit set
		I2C2->CR2 &= ~((0x7F << 16) | (0x3FF << 0));
    I2C2->CR2 |= ((2 << 16) | (0x57 << 1));
    I2C2->CR2 &= ~(1 << 10);
    I2C2->CR2 |= (1 << 13);

   	 
    // Should be breaking when TXIS is set
    while (1) {
   	 if (I2C2->ISR & (1 << 4)) {
   			 GPIOC->ODR ^= (1 << 6);
				 HAL_Delay(100);
   	 }
   	 if (I2C2->ISR & (1 << 1)) {
   		 break;
   	 }
    }
		
		//GPIOC->ODR ^= (1 << 6);
    
		// Setting it to heart rate mode
    I2C2->TXDR = 0x09;
		
    while (1) {
   	 if (I2C2->ISR & (1 << 4)) {
   			 GPIOC->ODR ^= (1 << 6);
				 HAL_Delay(100);
   	 }
   	 if (I2C2->ISR & (1 << 1)) {
   		 break;
   	 }
    }
		
		//GPIOC->ODR |= (1 << 7);
		// 010 is heart rate mode
    I2C2->TXDR = (0x02 << 0);
    
    while(!(I2C2->ISR & (1 << 6))) {
    }
    
		//GPIOC->ODR |= (1 << 8);
    I2C2->CR2 |= (1 << 14);

	// Set PPG Ready Enable bit
	// Set FIFO average register to reduce errors
	// Slave address is 0xBE, number of bytes to transmit is 2, write operation, start bit set
		I2C2->CR2 &= ~((0x7F << 16) | (0x3FF << 0));
    I2C2->CR2 |= ((2 << 16) | (0x57 << 1));
    I2C2->CR2 &= ~(1 << 10);
    I2C2->CR2 |= (1 << 13);
		
		//GPIOC->ODR &= ~((1 << 6) | (1 << 7) | (1 << 8));
		//GPIOC->ODR |= (1 << 9);
   	 
    // Should be breaking when TXIS is set
    while (1) {
   	 if (I2C2->ISR & (1 << 4)) {
   			 GPIOC->ODR ^= (1 << 6);
				 HAL_Delay(100);
   	 }
   	 if (I2C2->ISR & (1 << 1)) {
   		 break;
   	 }
    }
		
		//GPIOC->ODR |= (1 << 6);
    
		// Address where FIFO average is
    I2C2->TXDR = 0x08;
		
    while (1) {
   	 if (I2C2->ISR & (1 << 4)) {
   			 GPIOC->ODR ^= (1 << 6);
				 HAL_Delay(100);
   	 }
   	 if (I2C2->ISR & (1 << 1)) {
   		 break;
   	 }
    }
		
		//GPIOC->ODR |= (1 << 7);
		
		// Set bits for averaging samples
		//GPIOC->ODR |= (1 << 7);
    I2C2->TXDR = (0xE0 << 0);
    
    while(!(I2C2->ISR & (1 << 6))) {
    }
    
		//GPIOC->ODR |= (1 << 8);
    I2C2->CR2 |= (1 << 14);
		
		//GPIOC->ODR &= ~((1 << 6) | (1 << 7) | (1 << 8)); //| (1 << 9));
		
	// Set PPG Ready Enable bit
	// Slave address is 0xBE, number of bytes to transmit is 2, write operation, start bit set
		I2C2->CR2 &= ~((0x7F << 16) | (0x3FF << 0));
    I2C2->CR2 |= ((2 << 16) | (0x57 << 1));
    I2C2->CR2 &= ~(1 << 10);
    I2C2->CR2 |= (1 << 13);
		
		//GPIOC->ODR |= (1 << 6);
   	 
    // Should be breaking when TXIS is set
    while (1) {
   	 if (I2C2->ISR & (1 << 4)) {
   			 GPIOC->ODR ^= (1 << 6);
				 HAL_Delay(100);
   	 }
   	 if (I2C2->ISR & (1 << 1)) {
   		 break;
   	 }
    }
    
		// Address where PPG ready enable is
		//GPIOC->ODR |= (1 << 7);
    I2C2->TXDR = 0x02;
		
    while (1) {
   	 if (I2C2->ISR & (1 << 4)) {
   			 GPIOC->ODR ^= (1 << 6);
				 HAL_Delay(100);
   	 }
   	 if (I2C2->ISR & (1 << 1)) {
   		 break;
   	 }
    }
		
		// Set PPG Ready Enable bit
		// 010 is heart rate mode
		//GPIOC->ODR |= (1 << 8);
    I2C2->TXDR = (0x40 << 0);
    
    while(!(I2C2->ISR & (1 << 6))) {
    }
    
		//GPIOC->ODR |= (1 << 9);
    I2C2->CR2 |= (1 << 14);
		
		//GPIOC->ODR &= ~((1 << 6) | (1 << 7) | (1 << 8) );//| (1 << 9));
}



void setUpTimer(){
	// Set up timer to go off every so often
	// Refer to lab 3. Possibly Nathan's code if it is better commented
	TIM2->PSC = 7999;
	TIM2->ARR = 2000;
	TIM2->DIER |= (1 << 0);
	TIM2->CR1 |= (1 << 0);
	
	NVIC_EnableIRQ(TIM2_IRQn);
	NVIC_SetPriority(TIM2_IRQn, 3);
}

void setUpInterrupts(){
	GPIOB->MODER &= ~((1 << 19) | (1 << 18));
	GPIOB->OSPEEDR |= (1 << 19);
	GPIOB->PUPDR &= ~(1 << 18); 
	GPIOB->PUPDR |= (1 << 19);
	
	EXTI->IMR |= (1 << 9);
	EXTI->FTSR |= (1 << 9);
	SYSCFG->EXTICR[2] |= (1 << 4);
	
	NVIC_EnableIRQ(EXTI4_15_IRQn);
	NVIC_SetPriority(EXTI4_15_IRQn, 2);
}

void transmitChar(char c){
		while(1) {
		if (USART3->ISR & (1 << 7)) {
				break;
		}			
	}
	USART3->TDR = c;
}

void transmitString (char c[]){
	uint32_t i = 0;
	while (c[i] != '\0') {
		transmitChar(c[i]);
		i = i + 1;
	}
}

// This is the PPG interrupt from the heart rate chip
// PB0 is interrupt
void EXTI4_15_IRQHandler() {
	
		counter += 1;
		if(counter > 1){
			GPIOC->ODR |= (1 << 9);
		}
			// Attempting to read the interrupt
		I2C2->CR2 &= ~((0x7F << 16) | (0x3FF << 0));
    I2C2->CR2 |= ((1 << 16) | (0x57 << 1));
    I2C2->CR2 &= ~(1 << 10);
    I2C2->CR2 |= (1 << 13);
		
		//GPIOC->ODR |= (1 << 6);
   	 
    // Should be breaking when TXIS is set
    while (1) {
   	 if (I2C2->ISR & (1 << 4)) {
   			 GPIOC->ODR ^= (1 << 6);
				 HAL_Delay(100);
   	 }
   	 if (I2C2->ISR & (1 << 1)) {
   		 break;
   	 }
    }
		
		// Address where PPG ready enable is
		//GPIOC->ODR |= (1 << 7);
    I2C2->TXDR = (0x00 << 0);
    
    while(!(I2C2->ISR & (1 << 6))) {
    }
    
		//GPIOC->ODR |= (1 << 8);
    I2C2->CR2 |= (1 << 14);
		
		I2C2->CR2 &= ~((0x7F << 16) | (0x3FF << 0));
    I2C2->CR2 |= ((1 << 16) | (0x57 << 1));
    I2C2->CR2 |= (1 << 10);
    I2C2->CR2 |= (1 << 13);
		
		 while (1) {
   	 if (I2C2->ISR & (1 << 4)) {
   			 GPIOC->ODR ^= (1 << 6);
				 HAL_Delay(100);
   	 }
   	 if (I2C2->ISR & (1 << 2)) {
   		 break;
   	 }
    }
		
		//GPIOC->ODR &= ~(1 << 9);
		int test = I2C2->RXDR; 
	
		if(test & (1 << 0)){
			GPIOC->ODR |= (1 << 6);
			return;
		}
		else{
			GPIOC->ODR &= ~(1 << 6);
		}
			
	/* 
Reading data register:
	Set up device for write mode
	Send address of FIFO data register
	Set up device for read mode
	Read from FIFO data register
	Save data from read
	Read from FIFO data register
	Save data from read
	Read from FIFO data register
	Save data from read
	Set FIFO write pointer register to 0
	Set OVF counter register to 0
	Set FIFO read pointer register to 0
*/
	
	restartWriteCondition(1);
	waitForTxis();
	I2C2->TXDR = 0x07;
	waitForTC();
	restartReadCondition(4);
	WaitForRXNEorNACKF();
	uint32_t sample = I2C2->RXDR;
	WaitForRXNEorNACKF();
	sample = (sample << 8) | I2C2->RXDR;
	WaitForRXNEorNACKF();
	sample = (sample << 8) | I2C2->RXDR;
	WaitForRXNEorNACKF();
	sample = (sample << 8) | I2C2->RXDR;
	waitForTC();
	_sample = sample;
	
	//if(_sample > 150){
		//GPIOC->ODR ^= (1 << 7);
	//}
	
	//if(_sample > 2000){
		//GPIOC->ODR ^= (1 << 8);
	//}
	// Reset pointers
	restartWriteCondition(2);
	waitForTxis();
	I2C2->TXDR = 0x04; // Write pointer
	waitForTxis();
	I2C2->TXDR = 0x00;
	waitForTC();
	
	restartWriteCondition(2);
	waitForTxis();
	I2C2->TXDR = 0x05; // Overflow pointer
	waitForTxis();
	I2C2->TXDR = 0x00;
	waitForTC();
	
	restartWriteCondition(2);
	waitForTxis();
	I2C2->TXDR = 0x06; // Read pointer
	waitForTxis();
	I2C2->TXDR = 0x00;
	waitForTC();
	
	//GPIOC->ODR |= (1 << 8);
	
	EXTI->PR |= (1 << 9);
	if(counter > 1){
		
	GPIOC->ODR |= (1 << 7);
	}
	
}

void restartWriteCondition(uint16_t numBytes) {
	// Slave address is 0x57, number of bytes to transmit is numBytes, write operation, start bit set
	I2C2->CR2 &= ~((0x7F << 16) | (0x3FF << 0));
  I2C2->CR2 |= ((numBytes << 16) | (0x57 << 1));
  I2C2->CR2 &= ~(1 << 10);
  I2C2->CR2 |= (1 << 13);
}

void restartReadCondition(uint16_t numBytes) {
		// Slave address is 0x57, number of bytes to transmit is numBytes, read operation, start bit set
	I2C2->CR2 &= ~((0x7F << 16) | (0x3FF << 0));
  I2C2->CR2 |= ((numBytes << 16) | (0x57 << 1));
  I2C2->CR2 |= (1 << 10);
  I2C2->CR2 |= (1 << 13);
}

void waitForTxis() {
	   // Should be breaking when TXIS is set
    while (1) {
   	 if (I2C2->ISR & (1 << 4)) {
   			 GPIOC->ODR ^= (1 << 6);
				 HAL_Delay(100);
   	 }
   	 if (I2C2->ISR & (1 << 1)) {
   		 break;
   	 }
    }
}

void waitForTC() {
	while(!(I2C2->ISR & I2C_ISR_TC)) {
	}
}

void WaitForRXNEorNACKF(void)
{
	while(!(I2C2->ISR & ((1 << 2)|(1 << 4))))
	{
		// Do nothing, we are waiting
	}
}


// Send out to UART
void TIM2_IRQHandler() {
	
}
/* USER CODE END 4 */

/**
  * @brief  This function is executed in case of error occurrence.
  * @retval None
  */
void Error_Handler(void)
{
  /* USER CODE BEGIN Error_Handler_Debug */
  /* User can add his own implementation to report the HAL error return state */
  __disable_irq();
  while (1)
  {
  }
  /* USER CODE END Error_Handler_Debug */
}

#ifdef  USE_FULL_ASSERT
/**
  * @brief  Reports the name of the source file and the source line number
  *         where the assert_param error has occurred.
  * @param  file: pointer to the source file name
  * @param  line: assert_param error line source number
  * @retval None
  */
void assert_failed(uint8_t *file, uint32_t line)
{
  /* USER CODE BEGIN 6 */
  /* User can add his own implementation to report the file name and line number,
     ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */
  /* USER CODE END 6 */
}
#endif /* USE_FULL_ASSERT */

/************************ (C) COPYRIGHT STMicroelectronics *****END OF FILE****/
